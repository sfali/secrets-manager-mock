package com.alphasystem.aws.secretsmanager.routes

import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.alphasystem.aws.secretsmanager.repository.NitriteRepository

trait Routes {

  protected implicit val mat: Materializer
  protected implicit val log: LoggingAdapter
  protected implicit val repository: NitriteRepository

  protected lazy val _routes: Route =
    pathEndOrSingleSlash {
      concat(CreateSecretRoute().route)
    }
}
