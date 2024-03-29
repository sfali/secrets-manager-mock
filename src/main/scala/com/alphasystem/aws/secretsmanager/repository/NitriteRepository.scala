package com.alphasystem.aws.secretsmanager.repository

import java.time.OffsetDateTime

import com.alphasystem.aws.secretsmanager._
import com.alphasystem.aws.secretsmanager.model.{Errors, _}
import io.circe._
import io.circe.syntax._
import org.dizitart.no2._
import org.dizitart.no2.filters.Filters.{eq => feq, _}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

class NitriteRepository(settings: DBSettings) {

  import Document._
  import Errors._
  import IndexOptions._
  import IndexType._
  import NitriteRepository._

  private val db: Nitrite =
    Nitrite
      .builder()
      .compressed()
      .filePath(settings.filePath)
      .openOrCreate(settings.userName, settings.filePath)

  private val secretCollection: NitriteCollection = db.getCollection("secrets-manager")
  if (!secretCollection.hasIndex(ArnProperty))
    secretCollection.createIndex(ArnProperty, indexOptions(Fulltext))
  private val versionCollection = db.getCollection("secret-version")
  if (!versionCollection.hasIndex(VersionIdProperty)) {
    versionCollection.createIndex(VersionIdProperty, indexOptions(NonUnique))
    versionCollection.createIndex(VersionStageProperty, indexOptions(Fulltext))
  }
  private val versionCollectionOps = VersionCollectionOps(versionCollection)

  def createSecret(name: String,
                   versionId: Option[String] = None,
                   description: Option[String] = None,
                   kmsKeyId: Option[String] = None,
                   secretString: Option[String] = None,
                   secretBinary: Option[String] = None,
                   tag: Option[Tag] = None): SecretResponse = {
    val maybeSecretEntity =
      Try(describeSecret(name)) match {
        case Failure(_: ResourceNotFoundException) => None
        case Failure(ex) => throw ex
        case Success(secretEntity) => Some(secretEntity)
      }

    maybeSecretEntity match {
      case Some(secretEntity) =>
        versionId
          .map(vId => putSecretValue(name, vId, secretString, secretBinary))
          .getOrElse(throw ResourceExistsException(secretEntity.fullArn)) // TODO: find out AWS behavior
      case None => createNewSecret(name, versionId, description, kmsKeyId, secretString, secretBinary, tag)
    }
  }

  def updateSecret(secretId: String,
                   versionId: Option[String] = None,
                   description: Option[String] = None,
                   kmsKeyId: Option[String] = None,
                   secretString: Option[String] = None,
                   secretBinary: Option[String] = None,
                   versionStages: List[String] = Nil): SecretResponse = {
    val name = getNameFromSecretId(secretId)
    val docs = getSecretDocument(name)
    if (docs.isEmpty) throw ResourceNotFoundException()
    else if (docs.length > 1) throw new IllegalStateException(s"Found more than one record for $secretId.")
    var doc = docs.head.put(DescriptionProperty, description.orNull)
    doc = kmsKeyId.map(keyId => doc.put(KmsKeyIdProperty, keyId)).getOrElse(doc)
    val response =
      versionId
        .map(vId => putSecretValue(secretId, vId, secretString, secretBinary, versionStages))
        .getOrElse(SecretResponse(secretId, name))
    secretCollection.update(doc)
    response
  }

  private def createNewSecret(name: String,
                              versionId: Option[String],
                              description: Option[String],
                              kmsKeyId: Option[String],
                              secretString: Option[String],
                              secretBinary: Option[String],
                              tag: Option[Tag]) = {
    val (secret, property) = parseSecret(secretString, secretBinary)
    val arn = s"$name-$getRandomString"
    val creationDate = OffsetDateTime.now().toEpochSecond

    val doc = createDocument(NameProperty, name)
      .put(ArnProperty, arn)
      .put(DescriptionProperty, description.orNull)
      .put(KmsKeyIdProperty, kmsKeyId.getOrElse(DefaultKmsKeyId))
      .put(CreatedDateProperty, creationDate.toString)
      .put(LastChangedDateProperty, creationDate.toString)
      .put(TagsProperty, tag.asJson.pretty(NoSpaceWithDropNullValues))

    val maybeVersionDoc =
      versionId
        .map(vId => versionCollectionOps
          .createVersion(
            name = name,
            versionId = vId,
            secret = secret,
            secretProperty = property,
            creationDate = creationDate,
            versionStages = CurrentLabel :: Nil))

    secretCollection.insert(doc)
    maybeVersionDoc.foreach(versionCollection.insert(_))
    versionId.
      map(vId => SecretResponse(arn, name, Some(vId), CurrentLabel :: Nil))
      .getOrElse(SecretResponse(arn, name))
  }

  def getSecret(secretId: String,
                versionId: Option[String] = None,
                versionStage: Option[String] = None): SecretEntity = {

    val lookup = new Lookup()
    lookup.setLocalField(NameProperty)
    lookup.setForeignField(NameProperty)
    lookup.setTargetField(VersionsProperty)

    val name = getNameFromSecretId(secretId)
    val projections = new ListBuffer[Filter]()
    projections += feq(NameProperty, name)
    if (versionId.isDefined) {
      projections += feq(VersionIdProperty, versionId.get)
    }
    val stageToSearch = if (versionStage.isDefined) versionStage
    else if (versionStage.isEmpty && versionId.isEmpty) Some(CurrentLabel)
    else None
    if (stageToSearch.isDefined) {
      projections += text(VersionStageProperty, s"*${stageToSearch.get}*")
    }

    val documents =
      secretCollection.find(feq(NameProperty, name))
        .join(versionCollection.find(and(projections: _*)), lookup)
        .asScala.toList

    documents match {
      case Nil => throw ResourceNotFoundException()
      case document :: Nil => toSecretEntity(document)
      case _ => throw new IllegalStateException(s"Found more than one record for $secretId.")
    }
  }

  def putSecretValue(secretId: String,
                     versionId: String,
                     secretString: Option[String] = None,
                     secretBinary: Option[String] = None,
                     versionStages: List[String] = Nil): SecretResponse = {
    val name = getNameFromSecretId(secretId)
    val secretEntity = getSecretInternal(name) // if no resource found with given name then we should get exception here

    val (secret, property) = parseSecret(secretString, secretBinary)
    val maybeVersion = versionCollectionOps.findVersionById(name, versionId)
    maybeVersion match {
      case Some(version) => // version already exists
        // changes are not allowed to exiting versions
        val differentSecretString = secretString.isDefined && !version.secretString.forall(_ == secretString.get)
        val differentSecretBinary = secretBinary.isDefined && !version.secretBinary.forall(_ == secretBinary.get)
        if (differentSecretString || differentSecretBinary) {
          throw ResourceExistsException(secretEntity.fullArn, Some(versionId))
        } else {
          // existing version with no changes
          SecretResponse(secretEntity.arn, secretEntity.name, Some(version.versionId), version.stages)
        }
      case None =>
        // new version needs to be created
        addVersion(secretEntity.arn, name, versionId, secret, property, versionStages)

    }
  }

  def describeSecret(secretId: String): SecretEntity = {
    val name = getNameFromSecretId(secretId)
    getSecretInternal(name).copy(versions = versionCollectionOps.listVersions(name))
  }

  private def addVersion(arn: String,
                         name: String,
                         versionId: String,
                         secret: String,
                         property: String,
                         versionStages: List[String]) = {
    // retrieve current version
    val maybeCurrentVersion = versionCollectionOps.findCurrentVersion(name)

    // retrieve previous version
    val maybePreviousVersion = versionCollectionOps.findPreviousVersion(name)

    // if there is no current version then add current version
    var finalStages = if (maybeCurrentVersion.isEmpty || versionStages.isEmpty) versionStages :+ CurrentLabel
    else versionStages

    // if current version exists then remove previous label
    finalStages =
      if (maybeCurrentVersion.isDefined && finalStages.contains(CurrentLabel))
        finalStages.filterNot(_ == PreviousLabel)
      else finalStages

    // remove duplicates
    finalStages = finalStages.distinct

    // remove any existing stages except current (which is a special case)
    var stagesToRemove = finalStages.filterNot(_ == CurrentLabel)

    // move previous
    stagesToRemove =
      if (maybePreviousVersion.isDefined && finalStages.contains(CurrentLabel)) stagesToRemove :+ PreviousLabel
      else stagesToRemove

    // remove duplicate
    stagesToRemove = stagesToRemove.distinct

    var documents =
      stagesToRemove
        .map(stage => versionCollectionOps.removeStage(name, stage))
        .filter(_.isDefined)
        .map(_.get)

    // move current label to previous, if applicable
    documents =
      if (maybeCurrentVersion.isDefined && finalStages.contains(CurrentLabel))
        documents :+ versionCollectionOps.removeStage(name, CurrentLabel, PreviousLabel :: Nil).get
      else documents

    // remove previous label, if applicable
    documents =
      if (maybePreviousVersion.isDefined && finalStages.contains(PreviousLabel))
        documents :+ versionCollectionOps.removeStage(name, PreviousLabel).get
      else documents

    // now create new version
    val document = versionCollectionOps.createVersion(name = name, versionId = versionId, secret = secret,
      secretProperty = property, versionStages = finalStages)

    documents.map(versionCollection.update)
    versionCollection.insert(document)
    db.commit()
    SecretResponse(arn, name, Some(versionId), finalStages)
  }

  private def getSecretDocument(name: String) = secretCollection.find(feq(NameProperty, name)).toScalaList

  private def getSecretInternal(name: String) = {
    getSecretDocument(name) match {
      case Nil => throw ResourceNotFoundException()
      case document :: Nil => toSecretEntity(document, populateVersion = false)
      case _ => throw new IllegalStateException(s"Found more than one record for $name.")
    }
  }

  def close(): Unit = db.close()
}

object NitriteRepository {

  private val NoSpaceWithDropNullValues: Printer = Printer.noSpaces.copy(dropNullValues = true)

  def apply(settings: DBSettings): NitriteRepository = new NitriteRepository(settings)

  def apply()(implicit settings: Settings): NitriteRepository = NitriteRepository(settings.db)

}
