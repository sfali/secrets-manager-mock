package com.alphasystem.aws.secretsmanager.routes.model

import io.circe.{Decoder, Encoder}

case class DescribeSecretRequest(secretId: String)

object DescribeSecretRequest {
  implicit val DescribeSecretDecoder: Decoder[DescribeSecretRequest] =
    Decoder.forProduct1(nameA0 = "SecretId")(DescribeSecretRequest.apply)

  implicit val DescribeSecretEncoder: Encoder[DescribeSecretRequest] =
    Encoder.forProduct1(nameA0 = "SecretId")(value => value.secretId)
}
