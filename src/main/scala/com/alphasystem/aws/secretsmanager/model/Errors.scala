package com.alphasystem.aws.secretsmanager.model

object Errors {

  case object ResourceExistsException
    extends Exception("A resource with the ID you requested already exists.")

  case object ResourceNotFoundException
    extends Exception("We can't find the resource that you asked for.")

}
