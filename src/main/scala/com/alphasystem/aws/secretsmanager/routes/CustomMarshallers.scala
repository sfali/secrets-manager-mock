package com.alphasystem.aws.secretsmanager.routes

import akka.http.scaladsl.marshalling.Marshaller.fromToEntityMarshaller
import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller, ToResponseMarshaller}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import com.alphasystem.aws.secretsmanager.model.Errors
import com.alphasystem.aws.secretsmanager.routes.model.{CreateSecretResponse, GetSecretResponse, PutSecretValueResponse}
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport

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

  implicit val ResourceExistsExceptionResponse: ToResponseMarshaller[ResourceExistsException] =
    fromToEntityMarshaller[ResourceExistsException](BadRequest)

  implicit val ResourceNotFoundExceptionResponse: ToResponseMarshaller[ResourceNotFoundException] =
    fromToEntityMarshaller[ResourceNotFoundException](BadRequest)

  implicit val IllegalStateExceptionResponse: ToResponseMarshaller[IllegalStateException] =
    fromToEntityMarshaller[IllegalStateException](InternalServerError)

  implicit val GeneralExceptionResponse: ToResponseMarshaller[Exception] =
    fromToEntityMarshaller[Exception](ServiceUnavailable)
}
