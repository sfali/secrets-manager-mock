package com.alphasystem.aws.secretsmanager.routes.model

import io.circe.{Decoder, Encoder}

case class GetSecretRequest(secretId: String,
                            versionId: Option[String],
                            versionStage: Option[String])

object GetSecretRequest {
  implicit val CreateSecretRequestDecoder: Decoder[GetSecretRequest] =
    Decoder.forProduct3(
      nameA0 = "SecretId",
      nameA1 = "VersionId",
      nameA2 = "VersionStage")(GetSecretRequest.apply)

  implicit val CreateSecretRequestEncoder: Encoder[GetSecretRequest] =
    Encoder.forProduct3(
      nameA0 = "SecretId",
      nameA1 = "VersionId",
      nameA2 = "VersionStage")(value => (value.secretId, value.versionId, value.versionStage))
}
