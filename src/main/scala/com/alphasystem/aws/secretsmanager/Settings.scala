package com.alphasystem.aws.secretsmanager

import akka.actor.ActorSystem
import com.typesafe.config.Config

class Settings(config: Config) {
  def this(system: ActorSystem) = this(system.settings.config)

  object http {
    val host: String = config.getString("app.http.host")
    val port: Int = config.getInt("app.http.port")
  }

}

object Settings {
  def apply()(implicit system: ActorSystem): Settings = new Settings(system)
}
