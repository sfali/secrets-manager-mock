package com.alphasystem.aws.secretsmanager

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpHeader, HttpRequest}
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.alphasystem.aws.secretsmanager.model.Errors.InvalidRequestException
import com.alphasystem.aws.secretsmanager.model.{SecretEntity, SecretResponse, Target}
import com.alphasystem.aws.secretsmanager.routes.model.{CreateSecretResponse, GetSecretResponse}
import io.circe.Decoder
import io.circe.parser.decode

import scala.concurrent.Future

package object routes {

  import Directives._

  implicit class SecretResponseOps(src: SecretResponse) {
    def toCreateSecretResponse: CreateSecretResponse =
      CreateSecretResponse(s"$ARNPrefix${src.arn}", src.name, src.versionId)
  }

  implicit class SecretEntityOps(src: SecretEntity) {
    def toGetSecretResponse: GetSecretResponse = {
      val version = src.versions.headOption
      GetSecretResponse(
        arn = s"$ARNPrefix${src.arn}",
        name = src.name,
        createdDate = src.createdDate.toEpochSecond,
        versionId = version.map(_.versionId).orNull,
        secretString = version.flatMap(_.secretString),
        secretBinary = version.flatMap(_.secretBinary),
        versionStages = version.map(_.stages).getOrElse(Nil)
      )
    }
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
