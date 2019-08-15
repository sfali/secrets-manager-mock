package com.alphasystem.aws.secretsmanager.routes

import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives.{headerValue, post, _}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.alphasystem.aws.secretsmanager.model.Target
import com.alphasystem.aws.secretsmanager.repository.NitriteRepository
import com.alphasystem.aws.secretsmanager.routes.model.{GetSecretRequest, GetSecretResponse}

class GetSecretValueRoute private(log: LoggingAdapter, repository: NitriteRepository)
                                 (implicit mat: Materializer)
  extends CustomMarshallers {

  private def getResponse(request: GetSecretRequest): GetSecretResponse =
    repository.getSecret(request.secretId, request.versionId, request.versionStage).toGetSecretResponse

  def route: Route =
    (post & extractRequest & headerValue(extractTarget(Target.GetSecretValue))) {
      (request, _) =>
        completeRequest(extractEntity[GetSecretRequest, GetSecretResponse](request, log, getResponse), log)
    }
}

object GetSecretValueRoute {
  def apply()(implicit mat: Materializer,
              log: LoggingAdapter,
              repository: NitriteRepository): GetSecretValueRoute =
    new GetSecretValueRoute(log, repository)
}
