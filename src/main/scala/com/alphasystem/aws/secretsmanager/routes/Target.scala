package com.alphasystem.aws.secretsmanager.routes

import enumeratum.{CirceEnum, Enum, EnumEntry}

import scala.collection.immutable

sealed trait Target extends EnumEntry

object Target extends Enum[Target] with CirceEnum[Target] {
  override def values: immutable.IndexedSeq[Target] = findValues

  case object CreateSecret extends Target

  case object PutSecretValue extends Target

}
