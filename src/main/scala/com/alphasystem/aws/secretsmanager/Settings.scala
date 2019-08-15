package com.alphasystem.aws.secretsmanager

import akka.actor.ActorSystem
import com.typesafe.config.Config

class Settings(config: Config) {
  def this(system: ActorSystem) = this(system.settings.config)

  object http {
    val host: String = config.getString("app.http.host")
    val port: Int = config.getInt("app.http.port")
  }

  object db extends DBSettings {
    override val filePath: String = config.getString("app.db.file-path")
    override val userName: String = config.getString("app.db.user-name")
    override val password: String = config.getString("app.db.password")
  }

}

object Settings {
  def apply()(implicit system: ActorSystem): Settings = new Settings(system)
}
