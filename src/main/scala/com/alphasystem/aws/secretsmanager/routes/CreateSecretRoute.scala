package com.alphasystem.aws.secretsmanager.routes

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.alphasystem.aws.secretsmanager.model.Errors
import com.alphasystem.aws.secretsmanager.repository.NitriteRepository
import com.alphasystem.aws.secretsmanager.routes.model.CreateSecretRequest
import io.circe.parser._

import scala.util.{Failure, Success}

class CreateSecretRoute private(log: LoggingAdapter, repository: NitriteRepository)(implicit mat: Materializer)
  extends CustomMarshallers {

  import Errors._
  import mat.executionContext

  def route: Route =
    (post & extractRequest & headerValue(extractTarget(Target.CreateSecret))) {
      (request, _) =>
        onComplete(createSecret(request)) {
          case Success(value) => complete(value.toCreateSecretResponse)
          case Failure(ex: ResourceExistsException.type) =>
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

  private def createSecret(request: HttpRequest) =
    request
      .entity
      .dataBytes
      .map(_.utf8String)
      .runWith(Sink.head)
      .map(decode[CreateSecretRequest](_))
      .map {
        case Left(error) =>
          log.error(error, "Unable to decode request")
          throw InvalidRequestException
        case Right(value) => value
      }
      .map(entity => repository.createSecret(entity.name, entity.versionId,
        entity.description, entity.kmsKeyId, entity.secretString, entity.secretBinary, entity.tags))
}

object CreateSecretRoute {
  def apply()(implicit mat: Materializer, log: LoggingAdapter, repository: NitriteRepository): CreateSecretRoute =
    new CreateSecretRoute(log, repository)
}
