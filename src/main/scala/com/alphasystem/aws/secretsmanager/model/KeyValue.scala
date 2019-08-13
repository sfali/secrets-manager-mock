package com.alphasystem.aws.secretsmanager.model

import io.circe.{Decoder, Encoder}

case class KeyValue(key: String, value: String)

object KeyValue {
  implicit val KeyValueDecoder: Decoder[KeyValue] =
    Decoder.forProduct2("Key", "Value")(KeyValue.apply)

  implicit val KeyValueEncoder: Encoder[KeyValue] =
    Encoder.forProduct2("Key", "Value")(kv => (kv.key, kv.value))
}
