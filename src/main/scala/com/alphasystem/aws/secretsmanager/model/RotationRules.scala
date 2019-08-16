package com.alphasystem.aws.secretsmanager.model

import io.circe.{Decoder, Encoder}

case class RotationRules(afterDays: Int)

object RotationRules {
  implicit val RotationRulesDecoder: Decoder[RotationRules] =
    Decoder.forProduct1(nameA0 = "AutomaticallyAfterDays")(RotationRules.apply)

  implicit val RotationRulesEncoder: Encoder[RotationRules] =
    Encoder.forProduct1(nameA0 = "AutomaticallyAfterDays")(value => value.afterDays)
}
