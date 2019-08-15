package com.alphasystem.aws.secretsmanager.routes

import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives.{headerValue, post, _}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.alphasystem.aws.secretsmanager.model.Errors.ResourceNotFoundException
import com.alphasystem.aws.secretsmanager.model.Target
import com.alphasystem.aws.secretsmanager.repository.NitriteRepository
import com.alphasystem.aws.secretsmanager.routes.model.{GetSecretRequest, GetSecretResponse}

import scala.util.{Failure, Success}

class GetSecretValueRoute private(log: LoggingAdapter, repository: NitriteRepository)
                                 (implicit mat: Materializer)
  extends CustomMarshallers {

  private def getResponse(request: GetSecretRequest): GetSecretResponse =
    repository.getSecret(request.secretId, request.versionId, request.versionStage).toGetSecretResponse

  def route: Route =
    (post & extractRequest & headerValue(extractTarget(Target.GetSecretValue))) {
      (request, _) =>
        val eventualRequest = extractEntity[GetSecretRequest, GetSecretResponse](request, log, getResponse)
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
          case Success(value) => complete(value)
        }
    }
}

object GetSecretValueRoute {
  def apply()(implicit mat: Materializer,
              log: LoggingAdapter,
              repository: NitriteRepository): GetSecretValueRoute =
    new GetSecretValueRoute(log, repository)
}
