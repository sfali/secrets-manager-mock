package com.alphasystem.aws.secretsmanager

import java.util.Base64

import com.alphasystem.aws.secretsmanager.model.Errors.ResourceNotFoundException
import com.alphasystem.aws.secretsmanager.model.{SecretEntity, Version}
import org.dizitart.no2.Document

package object repository {

  val NameProperty = "Name"
  val ArnProperty = "ARN"
  val SecretStringProperty = "SecretString"
  val SecretBinaryProperty = "SecretBinary"
  val DescriptionProperty = "Description"
  val KmsKeyIdProperty = "KmsKeyId"
  val VersionIdProperty = "VersionId"
  val VersionStageProperty = "VersionStage"
  val DeletedDateProperty = "DeletedDate"
  val LastAccessedDateProperty = "LastAccessedDate"
  val LastChangedDateProperty = "LastChangedDate"
  val LastRotatedDateProperty = "LastRotatedDate"
  val CreatedDateProperty = "CreatedDate"
  val RotationEnabledProperty = "RotationEnabled"
  val RotationLambdaArnProperty = "RotationLambdaArn"
  val RotationRulesProperty = "RotationRules"
  val TagsProperty = "Tags"
  val VersionsProperty = "Versions"

  def toSecretEntity(document: Document, populateVersion: Boolean = true): SecretEntity = {
    val versions =
      if (populateVersion) {
        val maybeVersionsDocument = document.getOptionalForeignField(VersionsProperty)
        if (maybeVersionsDocument.isEmpty) {
          throw ResourceNotFoundException
        }
        val versionDocument = maybeVersionsDocument.get.head
        toVersion(versionDocument) :: Nil
      } else Nil

    SecretEntity(
      name = document.getString(NameProperty),
      arn = document.getString(ArnProperty),
      description = document.getOptionalString(DescriptionProperty),
      createdDate = document.getOffsetDateTime(CreatedDateProperty),
      versions = versions)
  }

  def toVersion(document: Document): Version = {
    val secretBinary = document.getOptionalString(SecretBinaryProperty)
    val secretString = document
      .getOptionalString(SecretStringProperty)
      .map(Base64.getDecoder.decode)
      .map(new String(_))
    val secret =
      if (secretBinary.isDefined) secretBinary.get
      else if (secretString.isDefined) secretString.get
      else ""

    Version(
      versionId = document.getString(VersionIdProperty),
      createdDate = document.getOffsetDateTime(CreatedDateProperty),
      secret = secret,
      stages = document.getOptionalString(VersionStageProperty).map(_.split(",").toList).getOrElse(Nil)
    )
  }

  def toVersion(documents: List[Document]): Option[Version] = {
    documents match {
      case Nil => None
      case document :: Nil => Some(toVersion(document))
      case head :: _ => throw new IllegalStateException(s"Found more than one record for ${head.getString(NameProperty)}")
    }
  }

  def parseSecret(secretString: Option[String] = None,
                  secretBinary: Option[String] = None): (String, String) = {
    if (secretString.isDefined) {
      (Base64.getEncoder.encodeToString(secretString.get.getBytes), SecretStringProperty)
    } else if (secretBinary.isDefined) {
      (secretBinary.get, SecretBinaryProperty)
    } else {
      throw new Exception("????")
    }
  }

}
