package com.alphasystem.aws.secretsmanager

import java.util.UUID

import io.circe.generic.auto._
import io.circe.syntax._

package object test {

  val toVersionId: String => String = (name: String) => UUID.nameUUIDFromBytes(name.getBytes).toString

  case class SecretModel(userName: String, password: String) {
    def toJson: String = this.asJson.noSpaces
  }
}
