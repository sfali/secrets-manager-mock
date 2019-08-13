package com.alphasystem.aws.secretsmanager.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import Directives._
import akka.event.LoggingAdapter

trait Routes {

  import Target._

  protected implicit val log: LoggingAdapter

  protected lazy val _routes: Route =
    pathEndOrSingleSlash {
      (headerValueByType[`X-Amz-Target`]() & headerValueByName("Authorization")) {
        (targetHeader, authorization) =>
          post {
            log.info("Authorization: {}", authorization)
            log.info("Target: {}", targetHeader.target)
            targetHeader.target match {
              case CreateSecret => complete("")
              case PutSecretValue => complete("")
              case _ => throw new IllegalArgumentException("Not implemented")
            }
          }
      }
    }
}
