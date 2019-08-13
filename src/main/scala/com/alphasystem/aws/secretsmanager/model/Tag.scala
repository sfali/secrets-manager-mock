package com.alphasystem.aws.secretsmanager.model

import io.circe.{Decoder, Encoder}

case class Tag(tags: List[KeyValue] = Nil)

object Tag {
  implicit val TagDecoder: Decoder[Tag] =
    Decoder.forProduct1("Tags")(Tag.apply)

  implicit val TagEncoder: Encoder[Tag] =
    Encoder.forProduct1("Tags")(t => (t.tags))
}
