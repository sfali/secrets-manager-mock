package com.alphasystem.aws.secretsmanager

import io.circe.generic.auto._
import io.circe.syntax._

package object test {

  case class SecretModel(userName: String, password: String) {

    def toJson: String = this.asJson.noSpaces
  }

}
