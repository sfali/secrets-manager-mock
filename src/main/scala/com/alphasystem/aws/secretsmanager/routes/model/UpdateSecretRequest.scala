package com.alphasystem.aws.secretsmanager.routes.model

import io.circe.{Decoder, Encoder}

case class UpdateSecretRequest(name: String,
                               versionId: String,
                               description: Option[String],
                               kmsKeyId: Option[String],
                               secretString: Option[String],
                               secretBinary: Option[String],
                               versionStages: Option[List[String]])

object UpdateSecretRequest {
  implicit val UpdateSecretRequestDecoder: Decoder[UpdateSecretRequest] =
    Decoder.forProduct7(
      nameA0 = "Name",
      nameA1 = "ClientRequestToken",
      nameA2 = "Description",
      nameA3 = "KmsKeyId",
      nameA4 = "SecretString",
      nameA5 = "SecretBinary",
      nameA6 = "VersionStages")(UpdateSecretRequest.apply)

  implicit val UpdateSecretRequestEncoder: Encoder[UpdateSecretRequest] =
    Encoder.forProduct7(
      nameA0 = "Name",
      nameA1 = "ClientRequestToken",
      nameA2 = "Description",
      nameA3 = "KmsKeyId",
      nameA4 = "SecretString",
      nameA5 = "SecretBinary",
      nameA6 = "VersionStages")(value => (value.name, value.versionId, value.description, value.kmsKeyId,
      value.secretString, value.secretBinary, value.versionStages))
}
