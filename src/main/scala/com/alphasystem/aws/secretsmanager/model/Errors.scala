package com.alphasystem.aws.secretsmanager.model

import io.circe.{Decoder, Encoder}
import io.circe.syntax._

object Errors {

  case object InvalidRequestException
    extends Exception("You provided a parameter value that is not valid for the current state of the resource.")

  case class ResourceExistsException(secretId: String) extends ErrorResponse {
    override val message: String = s"The operation failed because the secret $secretId already exists."
  }

  case class ResourceNotFoundException() extends ErrorResponse {
    override val message: String = "Secrets Manager can't find the specified secret."
  }

  sealed trait ErrorResponse extends Throwable {
    val errorType: String = getClass.getSimpleName
    val message: String

    override def getMessage: String = message

    def toJson: String = ApiError(errorType, message).asJson.noSpaces
  }

  case class ApiError(errorType: String, message: String)

  object ApiError {
    def apply(errorType: String, message: String): ApiError = new ApiError(errorType, message)

    implicit val ApiErrorDecoder: Decoder[ApiError] =
      Decoder.forProduct2("__type", "Message")(ApiError.apply)

    implicit val ApiErrorEncoder: Encoder[ApiError] =
      Encoder.forProduct2("__type", "Message")(error => (error.errorType, error.message))
  }


}
