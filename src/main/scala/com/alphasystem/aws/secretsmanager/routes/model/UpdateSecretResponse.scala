package com.alphasystem.aws.secretsmanager.routes.model

import io.circe.{Decoder, Encoder}

case class UpdateSecretResponse(arn: String,
                                name: String,
                                versionId: String,
                                versionStages: List[String])

object UpdateSecretResponse {
  implicit val UpdateSecretResponseDecoder: Decoder[UpdateSecretResponse] =
    Decoder.forProduct4(
      nameA0 = "ARN",
      nameA1 = "Name",
      nameA2 = "VersionId",
      nameA3 = "VersionStages")(UpdateSecretResponse.apply)

  implicit val UpdateSecretResponseEncoder: Encoder[UpdateSecretResponse] =
    Encoder.forProduct4(
      nameA0 = "ARN",
      nameA1 = "Name",
      nameA2 = "VersionId",
      nameA3 = "VersionStages")(value => (value.arn, value.name, value.versionId, value.versionStages))
}
