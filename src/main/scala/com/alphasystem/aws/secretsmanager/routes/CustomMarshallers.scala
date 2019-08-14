package com.alphasystem.aws.secretsmanager.routes

import akka.http.scaladsl.marshalling.Marshaller.fromToEntityMarshaller
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.StatusCodes
import com.alphasystem.aws.secretsmanager.model.Errors
import com.alphasystem.aws.secretsmanager.routes.model.CreateSecretResponse
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport

trait CustomMarshallers extends ErrorAccumulatingCirceSupport {

  import Errors._
  import StatusCodes._

  implicit val CreateSecretResponse: ToResponseMarshaller[CreateSecretResponse] =
    fromToEntityMarshaller[CreateSecretResponse](OK)

  implicit val ResourceExistsExceptionResponse: ToResponseMarshaller[ResourceExistsException.type] =
    fromToEntityMarshaller[ResourceExistsException.type](BadRequest)

  implicit val IllegalStateExceptionResponse: ToResponseMarshaller[IllegalStateException] =
    fromToEntityMarshaller[IllegalStateException](InternalServerError)

  implicit val GeneralExceptionResponse: ToResponseMarshaller[Exception] =
    fromToEntityMarshaller[Exception](ServiceUnavailable)
}
