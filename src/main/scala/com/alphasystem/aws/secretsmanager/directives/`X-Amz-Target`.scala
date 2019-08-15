package com.alphasystem.aws.secretsmanager.directives

import akka.http.scaladsl.model.headers.{ModeledCustomHeader, ModeledCustomHeaderCompanion}
import com.alphasystem.aws.secretsmanager.model.Target

import scala.util.Try

final class `X-Amz-Target`(value: Target) extends ModeledCustomHeader[`X-Amz-Target`] {
  override def companion: ModeledCustomHeaderCompanion[`X-Amz-Target`] = `X-Amz-Target`

  def target: Target = value

  override def value(): String = value.entryName

  override def renderInRequests(): Boolean = true

  override def renderInResponses(): Boolean = false
}

object `X-Amz-Target` extends ModeledCustomHeaderCompanion[`X-Amz-Target`] {
  override def name: String = "X-Amz-Target"

  override def parse(value: String): Try[`X-Amz-Target`] = {
    Try {
      val target = value.replace("secretsmanager.", "")
      new `X-Amz-Target`(Target.withName(target))
    }
  }
}