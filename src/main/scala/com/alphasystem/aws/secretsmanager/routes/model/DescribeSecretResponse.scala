package com.alphasystem.aws.secretsmanager.routes.model

import com.alphasystem.aws.secretsmanager.model.{KeyValue, RotationRules}
import io.circe.{Decoder, Encoder}

case class DescribeSecretResponse(arn: String,
                                  name: String,
                                  description: Option[String],
                                  kmsKeyId: String,
                                  deletedDate: Option[Long],
                                  lastAccessedDate: Option[Long],
                                  lastChangedDate: Option[Long],
                                  lastRotatedDate: Option[Long],
                                  rotationEnabled: Boolean,
                                  rotationLambdaArn: Option[String],
                                  rotationRules: Option[RotationRules],
                                  tags: Option[List[KeyValue]],
                                  versionIdsToStages: Map[String, List[String]])

object DescribeSecretResponse {
  implicit val DescribeSecretResponseDecoder: Decoder[DescribeSecretResponse] =
    Decoder.forProduct13(
      nameA0 = "ARN",
      nameA1 = "Name",
      nameA2 = "Description",
      nameA3 = "KmsKeyId",
      nameA4 = "DeletedDate",
      nameA5 = "LastAccessedDate",
      nameA6 = "LastChangedDate",
      nameA7 = "LastRotatedDate",
      nameA8 = "RotationEnabled",
      nameA9 = "RotationLambdaARN",
      nameA10 = "RotationRules",
      nameA11 = "Tags",
      nameA12 = "VersionIdsToStages")(DescribeSecretResponse.apply)

  implicit val DescribeSecretResponseEncoder: Encoder[DescribeSecretResponse] =
    Encoder.forProduct13(
      nameA0 = "ARN",
      nameA1 = "Name",
      nameA2 = "Description",
      nameA3 = "KmsKeyId",
      nameA4 = "DeletedDate",
      nameA5 = "LastAccessedDate",
      nameA6 = "LastChangedDate",
      nameA7 = "LastRotatedDate",
      nameA8 = "RotationEnabled",
      nameA9 = "RotationLambdaARN",
      nameA10 = "RotationRules",
      nameA11 = "Tags",
      nameA12 = "VersionIdsToStages")(value => (value.arn, value.name, value.description, value.kmsKeyId,
      value.deletedDate, value.lastAccessedDate, value.lastChangedDate, value.lastRotatedDate, value.rotationEnabled,
      value.rotationLambdaArn, value.rotationRules, value.tags, value.versionIdsToStages))
}
