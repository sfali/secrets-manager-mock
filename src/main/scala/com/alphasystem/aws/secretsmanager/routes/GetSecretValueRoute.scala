package com.alphasystem.aws.secretsmanager.routes

import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives.{headerValue, post}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.http.scaladsl.server.Directives._
import com.alphasystem.aws.secretsmanager.model.Errors.ResourceNotFoundException
import com.alphasystem.aws.secretsmanager.model.SecretEntity
import com.alphasystem.aws.secretsmanager.repository.NitriteRepository
import com.alphasystem.aws.secretsmanager.routes.model.GetSecretRequest

import scala.util.{Failure, Success}

class GetSecretValueRoute private(log: LoggingAdapter, repository: NitriteRepository)
                                 (implicit mat: Materializer)
  extends CustomMarshallers {

  private def getResponse(request: GetSecretRequest) =
    repository.getSecret(request.secretId, request.versionId, request.versionStage)

  def route: Route =
    (post & extractRequest & headerValue(extractTarget(Target.GetSecretValue))) {
      (request, _) =>
        val eventualRequest = extractEntity[GetSecretRequest, SecretEntity](request, log, getResponse)
        onComplete(eventualRequest) {
          case Failure(ex: ResourceNotFoundException) =>
            log.error(ex, "GetSecretValue-ResourceNotFoundException: Unable to create secret")
            complete(ex)
          case Failure(ex: IllegalStateException) =>
            log.error(ex, "GetSecretValue-IllegalStateException Unable to create secret")
            complete(ex)
          case Failure(ex) =>
            log.error(ex, "GetSecretValue-{} Unable to create secret", ex.getClass.getSimpleName)
            complete(ex)
          case Success(value) => complete(value.toGetSecretResponse)
        }
    }
}

object GetSecretValueRoute {
  def apply()(implicit mat: Materializer,
              log: LoggingAdapter,
              repository: NitriteRepository): GetSecretValueRoute =
    new GetSecretValueRoute(log, repository)
}
