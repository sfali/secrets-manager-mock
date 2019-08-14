package com.alphasystem.aws.secretsmanager.routes.model

import io.circe.{Decoder, Encoder}

case class GetSecretResponse(arn: String,
                             name: String,
                             createdDate: Long,
                             versionId: String,
                             secretString: Option[String] = None,
                             secretBinary: Option[String] = None,
                             versionStages: List[String] = Nil)

object GetSecretResponse {
  implicit val GetSecretResponseDecoder: Decoder[GetSecretResponse] =
    Decoder.forProduct7(
      nameA0 = "ARN",
      nameA1 = "Name",
      nameA2 = "CreatedDate",
      nameA3 = "VersionId",
      nameA4 = "SecretString",
      nameA5 = "SecretBinary",
      nameA6 = "VersionStages")(GetSecretResponse.apply)

  implicit val CreateSecretRequestEncoder: Encoder[GetSecretResponse] =
    Encoder.forProduct7(
      nameA0 = "ARN",
      nameA1 = "Name",
      nameA2 = "CreatedDate",
      nameA3 = "VersionId",
      nameA4 = "SecretString",
      nameA5 = "SecretBinary",
      nameA6 = "VersionStages")(value => (value.arn, value.name, value.createdDate, value.versionId, value.secretString,
      value.secretBinary, value.versionStages))
}
