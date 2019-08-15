package com.alphasystem.aws

import java.time.{Instant, OffsetDateTime, ZoneId}
import java.util

import org.dizitart.no2.{Cursor, Document}

import scala.collection.JavaConverters._
import scala.util.matching.Regex

package object secretsmanager {

  val DefaultRegion = "us-east-1"
  val DefaultAccountId: String = "890123456789"
  val CurrentLabel = "AWSCURRENT"
  val PendingLabel = "AWSPENDING"
  val PreviousLabel = "AWSPREVIOUS"
  val DefaultKmsKeyId = "aws/secretsmanager"
  val ARNPrefix: String = s"arn:aws:secretsmanager:$DefaultRegion:$DefaultAccountId:secret:"
  private val ArnRegex = "(arn:(aws[a-zA-Z-]*)?:secretsmanager:[a-z]{2}((-gov)|(-iso(b?)))?-[a-z]+-\\d{1}:\\d{12}:secret):([a-zA-Z0-9/_+=.@-]*)".r
  private val NameRegex = "([a-zA-Z0-9/_+=.@-]*)(-)([a-zA-Z0-9]{6})".r

  implicit class DocumentOps(src: Document) {
    def getString(key: String): String = src.get(key, classOf[String])

    def getBoolean(key: String): Boolean = src.get(key, classOf[Boolean])

    def getOptionalString(key: String): Option[String] = Option(getString(key))

    def getOffsetDateTime(key: String): OffsetDateTime =
      Instant.ofEpochSecond(getString(key).toLong).atZone(ZoneId.systemDefault()).toOffsetDateTime

    def getOptionalOffsetDateTime(key: String) = Option(getOffsetDateTime(key))

    def getOptionalForeignField(key: String): Option[List[Document]] =
      Option(src.get(key, classOf[util.HashSet[Document]])).map(_.asScala.toList)
  }

  implicit class CursorOps(src: Cursor) {
    def toScalaList: List[Document] = src.asScala.toList
  }

  private val AlphaNumericString: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz"
  private val TotalNumOfCharacters = 6

  def getRandomString: String = {
    val array = new Array[Char](TotalNumOfCharacters)
    for (n <- array.indices) {
      val index = (AlphaNumericString.length * math.random()).toInt
      array(n) = AlphaNumericString.charAt(index)
    }
    new String(array)
  }

  private def getRegexGroups(regex: Regex, str: String): List[String] =
    (for (m <- regex.findAllMatchIn(str)) yield m.subgroups).flatten.toList

  def getNameFromSecretId(secretId: String): String = {
    val arnGroups = getRegexGroups(ArnRegex, secretId)
    val nameWithSuffix =
      arnGroups match {
        case Nil => secretId // this should be the name
        case _ :+ tl => tl
      }

    val nameGroups = getRegexGroups(NameRegex, nameWithSuffix)
    nameGroups match {
      case Nil => nameWithSuffix // this should be the name
      case head :: _ => head
    }
  }

  trait DBSettings {
    val filePath: String
    val userName: String
    val password: String
  }
}
