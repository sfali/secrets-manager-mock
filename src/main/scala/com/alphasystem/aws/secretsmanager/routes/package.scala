package com.alphasystem.aws.secretsmanager

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpHeader, HttpRequest}
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.alphasystem.aws.secretsmanager.model.Errors.InvalidRequestException
import com.alphasystem.aws.secretsmanager.model.SecretResponse
import com.alphasystem.aws.secretsmanager.routes.model.CreateSecretResponse
import io.circe.Decoder
import io.circe.parser.decode

import scala.concurrent.Future

package object routes {

  import Directives._

  implicit class SecretResponseOps(src: SecretResponse) {
    def toCreateSecretResponse: CreateSecretResponse =
      CreateSecretResponse(s"$ARNPrefix${src.arn}", src.name, src.versionId)
  }

  def extractTarget(value: Target): HttpHeader => Option[String] = {
    case t: RawHeader =>
      if (t.name == "X-Amz-Target") {
        `X-Amz-Target`.parse(t.value).toOption match {
          case Some(target) =>
            if (target.target == value) {
              Some(value.entryName)
            } else None
          case None => None
        }
      } else None
    case _ => None
  }

  def extractEntity[Request, Response](request: HttpRequest,
                                       log: LoggingAdapter,
                                       fun: Request => Response)
                                      (implicit mat: Materializer,
                                       decoder: Decoder[Request]): Future[Response] = {
    import mat.executionContext
    request
      .entity
      .dataBytes
      .map(_.utf8String)
      .runWith(Sink.head)
      .map(decode[Request](_))
      .map {
        case Left(error) =>
          log.error(error, "Unable to decode request")
          throw InvalidRequestException
        case Right(value) => value
      }
      .map(fun(_))
  }
}
