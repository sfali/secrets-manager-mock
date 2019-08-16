package com.alphasystem.aws.secretsmanager.routes

import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.alphasystem.aws.secretsmanager.model.Target
import com.alphasystem.aws.secretsmanager.repository.NitriteRepository
import com.alphasystem.aws.secretsmanager.routes.model.{DescribeSecretRequest, DescribeSecretResponse}

class DescribeSecretRoute private(log: LoggingAdapter, repository: NitriteRepository)
                                 (implicit mat: Materializer)
  extends CustomMarshallers {

  private def getResponse(request: DescribeSecretRequest): DescribeSecretResponse =
    repository.describeSecret(request.secretId).toDescribeSecretResponse

  def route: Route =
    (post & extractRequest & headerValue(extractTarget(Target.DescribeSecret))) {
      (request, _) => {
        completeRequest(extractEntity[DescribeSecretRequest, DescribeSecretResponse](request, log, getResponse), log)
      }
    }
}

object DescribeSecretRoute {
  def apply()(implicit mat: Materializer, log: LoggingAdapter, repository: NitriteRepository): DescribeSecretRoute =
    new DescribeSecretRoute(log, repository)
}
