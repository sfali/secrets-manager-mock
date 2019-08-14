package com.alphasystem.aws.secretsmanager

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.stream.{ActorMaterializer, Materializer}
import com.alphasystem.aws.secretsmanager.repository.NitriteRepository
import com.alphasystem.aws.secretsmanager.routes.Routes
import com.typesafe.config.ConfigFactory

object Main extends HttpApp with App with Routes {

  private val config = ConfigFactory.load()
  private implicit val system: ActorSystem = ActorSystem(config.getString("app.name"), config)
  protected override implicit val mat: Materializer = ActorMaterializer()
  private implicit val settings: Settings = Settings()
  override protected implicit val log: LoggingAdapter = system.log
  override protected implicit val repository: NitriteRepository = NitriteRepository("secrets-manager.db")

  override protected def routes: Route = _routes

  Main.startServer(settings.http.host, settings.http.port, system)
}
