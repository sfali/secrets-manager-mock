package com.alphasystem.aws.secretsmanager.routes

import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.alphasystem.aws.secretsmanager.model.Errors.ResourceExistsException
import com.alphasystem.aws.secretsmanager.model.{SecretResponse, Target}
import com.alphasystem.aws.secretsmanager.repository.NitriteRepository
import com.alphasystem.aws.secretsmanager.routes.model.{PutSecretValueRequest, PutSecretValueResponse}

import scala.util.{Failure, Success}

class PutSecretValueRoute private(log: LoggingAdapter, repository: NitriteRepository)
                                 (implicit mat: Materializer)
  extends CustomMarshallers {

  private def getResponse(request: PutSecretValueRequest): PutSecretValueResponse =
    repository.putSecretValue(request.secretId, request.versionId, request.secretString, request.secretBinary,
      request.versionStages).toPutSecretValueResponse

  def route: Route =
    (post & extractRequest & headerValue(extractTarget(Target.PutSecretValue))) {
      (request, _) => {
        val eventualRequest = extractEntity[PutSecretValueRequest, PutSecretValueResponse](request, log, getResponse)
        onComplete(eventualRequest) {
          case Success(value) => complete(value)
          case Failure(ex: ResourceExistsException) =>
            log.error(ex, "CreateSecretRequest-ResourceExistsException: Unable to create secret")
            complete(ex)
          case Failure(ex: IllegalStateException) =>
            log.error(ex, "CreateSecretRequest-IllegalStateException Unable to create secret")
            complete(ex)
          case Failure(ex) =>
            log.error(ex, "CreateSecretRequest-{} Unable to create secret",
              ex.getClass.getSimpleName)
            complete(ex)
        }
      }
    }
}

object PutSecretValueRoute {
  def apply()(implicit mat: Materializer, log: LoggingAdapter, repository: NitriteRepository): PutSecretValueRoute =
    new PutSecretValueRoute(log, repository)
}
