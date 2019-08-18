package com.alphasystem.aws.secretsmanager

import java.net.URI

import software.amazon.awssdk.auth.credentials._
import software.amazon.awssdk.regions.Region

package object it {

  implicit class SettingsOps(src: Settings) {
    val awsSettings: AWSSettings = new AWSSettings {
      override val region: Region = Region.of(src.config.getString("app.aws.region"))
      override val credentialsProvider: AwsCredentialsProvider =
        src.config.getString("app.aws.credentials.provider") match {
          case "default" => DefaultCredentialsProvider.create()
          case "anon" => AnonymousCredentialsProvider.create()
          case "static" =>
            val aki = src.config.getString("app.aws.credentials.access-key-id")
            val sak = src.config.getString("app.aws.credentials.secret-access-key")
            val tokenPath = "app.aws.credentials.token"
            val creds = if (src.config.hasPath(tokenPath)) {
              AwsSessionCredentials.create(aki, sak, src.config.getString(tokenPath))
            } else {
              AwsBasicCredentials.create(aki, sak)
            }
            StaticCredentialsProvider.create(creds)
          case _ => DefaultCredentialsProvider.create()
        }
    }
    object secretsManager {
      val endPoint: URI = new URI(src.config.getString("app.secrets-manager.end-point"))
    }
  }

  trait AWSSettings {
    val region: Region
    val credentialsProvider: AwsCredentialsProvider
  }

}
