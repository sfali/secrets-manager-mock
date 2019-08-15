package com.alphasystem.aws.secretsmanager.routes

import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.alphasystem.aws.secretsmanager.model.Target
import com.alphasystem.aws.secretsmanager.repository.NitriteRepository
import com.alphasystem.aws.secretsmanager.routes.model.{CreateSecretRequest, CreateSecretResponse}

class CreateSecretRoute private(log: LoggingAdapter, repository: NitriteRepository)(implicit mat: Materializer)
  extends CustomMarshallers {

  private def getResponse(entity: CreateSecretRequest): CreateSecretResponse =
    repository.createSecret(entity.name, entity.versionId, entity.description, entity.kmsKeyId, entity.secretString,
      entity.secretBinary, entity.tags).toCreateSecretResponse

  def route: Route =
    (post & extractRequest & headerValue(extractTarget(Target.CreateSecret))) {
      (request, _) =>
        completeRequest(extractEntity[CreateSecretRequest, CreateSecretResponse](request, log, getResponse), log)
    }
}

object CreateSecretRoute {
  def apply()(implicit mat: Materializer, log: LoggingAdapter, repository: NitriteRepository): CreateSecretRoute =
    new CreateSecretRoute(log, repository)
}
