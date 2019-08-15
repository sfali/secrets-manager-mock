package com.alphasystem.aws.secretsmanager.routes.model

import io.circe.{Decoder, Encoder}

case class PutSecretValueRequest(secretId: String,
                                 versionId: String,
                                 secretString: Option[String],
                                 secretBinary: Option[String],
                                 versionStages: Option[List[String]])

object PutSecretValueRequest {
  implicit val PutSecretRequestDecoder: Decoder[PutSecretValueRequest] =
    Decoder.forProduct5(
      nameA0 = "SecretId",
      nameA1 = "ClientRequestToken",
      nameA2 = "SecretString",
      nameA3 = "SecretBinary",
      nameA4 = "VersionStages")(PutSecretValueRequest.apply)

  implicit val PutSecretRequestEncoder: Encoder[PutSecretValueRequest] =
    Encoder.forProduct5(
      nameA0 = "SecretId",
      nameA1 = "ClientRequestToken",
      nameA2 = "SecretString",
      nameA3 = "SecretBinary",
      nameA4 = "VersionStages")(value => (value.secretId, value.versionId, value.secretString, value.secretBinary,
      value.versionStages))
}
