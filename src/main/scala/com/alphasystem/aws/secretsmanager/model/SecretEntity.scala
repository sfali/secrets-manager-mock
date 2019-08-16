package com.alphasystem.aws.secretsmanager.model

import com.alphasystem.aws.secretsmanager.{DefaultKmsKeyId, ARNPrefix}
import java.time.OffsetDateTime

case class SecretEntity(name: String,
                        arn: String,
                        description: Option[String] = None,
                        kmsKeyId: String = DefaultKmsKeyId,
                        createdDate: OffsetDateTime = OffsetDateTime.now(),
                        lastChangedDate: OffsetDateTime = OffsetDateTime.now(),
                        deletedDate: Option[OffsetDateTime] = None,
                        lastAccessedDate: Option[OffsetDateTime] = None,
                        lastRotatedDate: Option[OffsetDateTime] = None,
                        rotationEnabled: Boolean = false,
                        rotationLambdaArn: Option[String] = None,
                        rotationRules: Option[RotationRules] = None,
                        versions: List[Version] = Nil,
                        tags: Option[Tag] = None) {
  def fullArn = s"$ARNPrefix$arn"

  def versionIdsToStages(includeDeprecated: Boolean = false): Map[String, List[String]] =
    versions
      .groupBy(_.versionId)
      .map {
        case (versionId, _versions) =>
          val allStages = _versions.flatMap(_.stages)
          val stages =
            if (includeDeprecated) allStages
            else allStages.filterNot(_.isEmpty)
          (versionId, stages)
      }
}
