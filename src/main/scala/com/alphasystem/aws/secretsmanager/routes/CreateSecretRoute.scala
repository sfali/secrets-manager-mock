package com.alphasystem.aws.secretsmanager.routes

import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.alphasystem.aws.secretsmanager.model.{Errors, Target}
import com.alphasystem.aws.secretsmanager.repository.NitriteRepository
import com.alphasystem.aws.secretsmanager.routes.model.{CreateSecretRequest, CreateSecretResponse}

import scala.util.{Failure, Success}

class CreateSecretRoute private(log: LoggingAdapter, repository: NitriteRepository)(implicit mat: Materializer)
  extends CustomMarshallers {

  import Errors._

  private def getResponse(entity: CreateSecretRequest): CreateSecretResponse =
    repository.createSecret(entity.name, entity.versionId, entity.description, entity.kmsKeyId, entity.secretString,
      entity.secretBinary, entity.tags).toCreateSecretResponse

  def route: Route =
    (post & extractRequest & headerValue(extractTarget(Target.CreateSecret))) {
      (request, _) =>
        val eventualRequest = extractEntity[CreateSecretRequest, CreateSecretResponse](request, log, getResponse)
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

object CreateSecretRoute {
  def apply()(implicit mat: Materializer, log: LoggingAdapter, repository: NitriteRepository): CreateSecretRoute =
    new CreateSecretRoute(log, repository)
}
