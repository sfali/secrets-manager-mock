package com.alphasystem.aws.secretsmanager.routes.model

import io.circe.{Decoder, Encoder}

case class CreateSecretResponse(arn: String, name: String, versionId: String)

object CreateSecretResponse {
  implicit val CreateSecretResponseDecoder: Decoder[CreateSecretResponse] =
    Decoder.forProduct3(
      nameA0 = "ARN",
      nameA1 = "Name",
      nameA2 = "VersionId")(CreateSecretResponse.apply)

  implicit val CreateSecretResponseEncoder: Encoder[CreateSecretResponse] =
    Encoder.forProduct3(
      nameA0 = "ARN",
      nameA1 = "Name",
      nameA2 = "VersionId")(value => (value.arn, value.name, value.versionId))
}
