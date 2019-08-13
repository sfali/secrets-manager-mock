package com.alphasystem.aws.secretsmanager.model

import com.alphasystem.aws.secretsmanager.DefaultKmsKeyId
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
  def versionIdsToStages: Map[String, List[String]] =
    versions
      .groupBy(_.versionId)
      .map {
        case (versionId, _versions) => (versionId, _versions.flatMap(_.stages))
      }
}
