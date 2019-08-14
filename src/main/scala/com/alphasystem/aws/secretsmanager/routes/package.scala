package com.alphasystem.aws.secretsmanager

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.RawHeader
import com.alphasystem.aws.secretsmanager.model.SecretResponse
import com.alphasystem.aws.secretsmanager.routes.model.CreateSecretResponse

package object routes {

  import Directives._

  implicit class SecretResponseOps(src: SecretResponse) {
    def toCreateSecretResponse: CreateSecretResponse =
      CreateSecretResponse(s"${ARNPrefix}${src.arn}", src.name, src.versionId)
  }

  def extractTarget(value: Target): HttpHeader => Option[String] = {
    case t: RawHeader =>
      if (t.name == "X-Amz-Target") {
        `X-Amz-Target`.parse(t.value).toOption match {
          case Some(target) =>
            if (target.target == value) {
              Some(value.entryName)
            } else None
          case None => None
        }
      } else None
    case _ => None
  }
}
