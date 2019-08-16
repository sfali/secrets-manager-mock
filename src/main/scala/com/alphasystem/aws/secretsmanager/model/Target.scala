package com.alphasystem.aws.secretsmanager.model

import enumeratum.{CirceEnum, Enum, EnumEntry}

import scala.collection.immutable

sealed trait Target extends EnumEntry

object Target extends Enum[Target] with CirceEnum[Target] {
  override def values: immutable.IndexedSeq[Target] = findValues

  case object CancelRotateSecret extends Target

  case object CreateSecret extends Target

  case object DeleteResourcePolicy extends Target

  case object DescribeSecret extends Target

  case object DeleteSecret extends Target

  case object GetRandomPassword extends Target

  case object GetResourcePolicy extends Target

  case object GetSecretValue extends Target

  case object ListSecrets extends Target

  case object ListSecretVersionIds extends Target

  case object PutResourcePolicy extends Target

  case object RotateSecret extends Target

  case object PutSecretValue extends Target

  case object RestoreSecret extends Target

  case object TagResource extends Target

  case object UntagResource extends Target

  case object UpdateSecret extends Target

  case object UpdateSecretVersionStag extends Target

}
