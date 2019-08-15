package com.alphasystem.aws.secretsmanager.routes

import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.alphasystem.aws.secretsmanager.model.Target
import com.alphasystem.aws.secretsmanager.repository.NitriteRepository
import com.alphasystem.aws.secretsmanager.routes.model.{PutSecretValueRequest, PutSecretValueResponse}

class PutSecretValueRoute private(log: LoggingAdapter, repository: NitriteRepository)
                                 (implicit mat: Materializer)
  extends CustomMarshallers {

  private def getResponse(request: PutSecretValueRequest): PutSecretValueResponse =
    repository.putSecretValue(request.secretId, request.versionId, request.secretString, request.secretBinary,
      request.versionStages.getOrElse(Nil)).toPutSecretValueResponse

  def route: Route =
    (post & extractRequest & headerValue(extractTarget(Target.PutSecretValue))) {
      (request, _) => {
        completeRequest(extractEntity[PutSecretValueRequest, PutSecretValueResponse](request, log, getResponse), log)
      }
    }
}

object PutSecretValueRoute {
  def apply()(implicit mat: Materializer, log: LoggingAdapter, repository: NitriteRepository): PutSecretValueRoute =
    new PutSecretValueRoute(log, repository)
}
