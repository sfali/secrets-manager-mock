package com.alphasystem.aws.secretsmanager.routes

import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.Marshaller.fromToEntityMarshaller
import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller, ToResponseMarshaller}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.alphasystem.aws.secretsmanager.model.Errors
import com.alphasystem.aws.secretsmanager.routes.model.{CreateSecretResponse, DescribeSecretResponse, GetSecretResponse, PutSecretValueResponse}
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport

import scala.concurrent.Future
import scala.util.Failure

trait CustomMarshallers extends ErrorAccumulatingCirceSupport {

  import Errors._
  import StatusCodes._

  private val defaultContentType = ContentTypes.`application/json`

  private def errorResponseMarshaller[T]: ToEntityMarshaller[ErrorResponse] =
    Marshaller.withFixedContentType(defaultContentType) { result =>
      HttpEntity(defaultContentType, result.toJson)
    }

  implicit val ResourceExistsExceptionMarshaller: ToEntityMarshaller[ResourceExistsException] =
    errorResponseMarshaller[ResourceExistsException]

  implicit val ResourceNotFoundExceptionMarshaller: ToEntityMarshaller[ResourceNotFoundException] =
    errorResponseMarshaller[ResourceNotFoundException]

  implicit val CreateSecretResponseMarshaller: ToResponseMarshaller[CreateSecretResponse] =
    fromToEntityMarshaller[CreateSecretResponse](OK)

  implicit val GetSecretResponseMarshaller: ToResponseMarshaller[GetSecretResponse] =
    fromToEntityMarshaller[GetSecretResponse](OK)

  implicit val PutSecretValueResponseMarshaller: ToResponseMarshaller[PutSecretValueResponse] =
    fromToEntityMarshaller[PutSecretValueResponse](OK)

  implicit val DescribeSecretResponseMarshaller: ToResponseMarshaller[DescribeSecretResponse] =
    fromToEntityMarshaller[DescribeSecretResponse](OK)

  implicit val ResourceExistsExceptionResponse: ToResponseMarshaller[ResourceExistsException] =
    fromToEntityMarshaller[ResourceExistsException](BadRequest)

  implicit val ResourceNotFoundExceptionResponse: ToResponseMarshaller[ResourceNotFoundException] =
    fromToEntityMarshaller[ResourceNotFoundException](BadRequest)

  implicit val IllegalStateExceptionResponse: ToResponseMarshaller[IllegalStateException] =
    fromToEntityMarshaller[IllegalStateException](InternalServerError)

  implicit val GeneralExceptionResponse: ToResponseMarshaller[Exception] =
    fromToEntityMarshaller[Exception](ServiceUnavailable)

  protected def completeRequest[T](eventualResponse: Future[T], log: LoggingAdapter)
                                  (implicit r: ToResponseMarshaller[T]): Route =
    onComplete(eventualResponse) {
      case util.Success(value) => complete(value)
      case Failure(ex: ResourceNotFoundException) =>
        log.error(ex, "CreateSecretRequest-ResourceNotFoundException")
        complete(ex)
      case Failure(ex: ResourceExistsException) =>
        log.error(ex, "CreateSecretRequest-ResourceExistsException")
        complete(ex)
      case Failure(ex: IllegalStateException) =>
        log.error(ex, "CreateSecretRequest-IllegalStateException")
        complete(ex)
      case Failure(ex) =>
        log.error(ex, "CreateSecretRequest-{} Unable to create secret",
          ex.getClass.getSimpleName)
        complete(ex)
    }
}
