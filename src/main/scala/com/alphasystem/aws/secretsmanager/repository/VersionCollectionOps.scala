package com.alphasystem.aws.secretsmanager.repository

import java.time.OffsetDateTime

import com.alphasystem.aws.secretsmanager._
import com.alphasystem.aws.secretsmanager.model.Version
import org.dizitart.no2.filters.Filters.{eq => feq, _}
import org.dizitart.no2.{Document, NitriteCollection}

class VersionCollectionOps(collection: NitriteCollection) {

  def createVersion(name: String,
                    versionId: String,
                    secret: String,
                    secretProperty: String,
                    creationDate: Long = OffsetDateTime.now().toEpochSecond,
                    versionStages: List[String] = Nil): Document =
    Document
      .createDocument(NameProperty, name)
      .put(VersionIdProperty, versionId)
      .put(secretProperty, secret)
      .put(CreatedDateProperty, creationDate.toString)
      .put(VersionStageProperty, versionStages.mkString(","))

  def findVersionById(name: String, versionId: String): Option[Version] =
    collection.find(and(feq(NameProperty, name), feq(VersionIdProperty, versionId)))
      .toScalaList match {
      case Nil => None
      case document :: Nil => Some(toVersion(document))
      case _ => throw new IllegalStateException(s"Found more than one record for $name/$versionId.")
    }

  def findVersionByStage(name: String, versionStage: String): Option[Version] =
    collection.find(and(feq(NameProperty, name), text(VersionStageProperty, s"*$versionStage*")))
      .toScalaList match {
      case Nil => None
      case document :: Nil => Some(toVersion(document))
      case _ => throw new IllegalStateException(s"Found more than one record for $name/$versionStage.")
    }

  def findCurrentVersion(name: String): Option[Version] = findVersionByStage(name, CurrentLabel)

  def findPreviousVersion(name: String): Option[Version] = findVersionByStage(name, PreviousLabel)

  def removeStage(name: String, stageToRemove: String, stagesToAdd: List[String] = Nil): Option[Document] = {
    collection.find(and(feq(NameProperty, name), text(VersionStageProperty, s"*$stageToRemove*")))
      .toScalaList match {
      case document :: Nil =>
        val currentStages = document.getString(VersionStageProperty)
        val newStages =
          if (Option(currentStages).isEmpty) {
            stagesToAdd
          } else currentStages.split(",").filterNot(_ == stageToRemove).toList ::: stagesToAdd

        val stages = if (newStages.isEmpty) null else newStages.mkString(",")

        Some(
          document
            .put(VersionStageProperty, stages)
            .put(LastAccessedDateProperty, OffsetDateTime.now().toEpochSecond.toString)
        )
      case Nil => None
      case _ => throw new IllegalStateException(s"Found more than one record for $name/$stageToRemove.")
    }
  }

  def listVersions(name: String): List[Version] = {
    collection.find(feq(NameProperty, name)).toScalaList.map(toVersion)
  }
}

object VersionCollectionOps {
  def apply(collection: NitriteCollection): VersionCollectionOps = new VersionCollectionOps(collection)
}
