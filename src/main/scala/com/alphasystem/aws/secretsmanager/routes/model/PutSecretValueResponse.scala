package com.alphasystem.aws.secretsmanager.routes.model

import io.circe.{Decoder, Encoder}

case class PutSecretValueResponse(arn: String,
                                  name: String,
                                  versionId: Option[String],
                                  versionStages: List[String])

object PutSecretValueResponse {
  implicit val PutSecretResponseDecoder: Decoder[PutSecretValueResponse] =
    Decoder.forProduct4(
      nameA0 = "ARN",
      nameA1 = "Name",
      nameA2 = "VersionId",
      nameA3 = "VersionStages")(PutSecretValueResponse.apply)

  implicit val PutSecretResponseEncoder: Encoder[PutSecretValueResponse] =
    Encoder.forProduct4(
      nameA0 = "ARN",
      nameA1 = "Name",
      nameA2 = "VersionId",
      nameA3 = "VersionStages")(value => (value.arn, value.name, value.versionId, value.versionStages))
}
