package com.alphasystem.aws.secretsmanager.routes.model

import com.alphasystem.aws.secretsmanager.model.Tag
import io.circe.{Decoder, Encoder}

case class CreateSecretRequest(name: String,
                               versionId: String,
                               description: Option[String],
                               kmsKeyId: Option[String],
                               secretString: Option[String],
                               secretBinary: Option[String],
                               tags: Option[Tag])

object CreateSecretRequest {
  implicit val CreateSecretRequestDecoder: Decoder[CreateSecretRequest] =
    Decoder.forProduct7(
      nameA0 = "Name",
      nameA1 = "ClientRequestToken",
      nameA2 = "Description",
      nameA3 = "KmsKeyId",
      nameA4 = "SecretString",
      nameA5 = "SecretBinary",
      nameA6 = "Tags")(CreateSecretRequest.apply)

  implicit val CreateSecretRequestEncoder: Encoder[CreateSecretRequest] =
    Encoder.forProduct7(
      nameA0 = "Name",
      nameA1 = "ClientRequestToken",
      nameA2 = "Description",
      nameA3 = "KmsKeyId",
      nameA4 = "SecretString",
      nameA5 = "SecretBinary",
      nameA6 = "Tags")(value => (value.name, value.versionId, value.description, value.kmsKeyId, value.secretString,
      value.secretBinary, value.tags))
}
