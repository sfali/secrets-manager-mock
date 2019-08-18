package com.alphasystem.aws.secretsmanager

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.alphasystem.aws.secretsmanager.repository.NitriteRepository
import com.alphasystem.aws.secretsmanager.routes.Routes

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SecretsManagerServer(implicit system: ActorSystem,
                           settings: Settings,
                           val mat: Materializer,
                           val repository: NitriteRepository)
  extends Routes {

  override protected implicit val log: LoggingAdapter = system.log

  private val http = Http()

  private var bind: Http.ServerBinding = _

  def start(): Unit = {
    val httpSettings = settings.http
    val host = httpSettings.host
    val port = httpSettings.port
    bind = Await.result(http.bindAndHandle(_routes, host, port), Duration.Inf)
    log.info("Server online at http://{}:{}", host, port)
  }

  def shutdown(): Unit = {
    import system.dispatcher
    log.info("Stopping server")
    repository.close()
    val stopped =
      for {
        _ <- bind.unbind()
        _ <- http.shutdownAllConnectionPools()
        _ <- system.terminate()
      } yield ()
    Await.result(stopped, Duration.Inf)
  }
}

object SecretsManagerServer {
  def apply()(implicit system: ActorSystem,
              settings: Settings,
              mat: Materializer,
              repository: NitriteRepository): SecretsManagerServer =
    new SecretsManagerServer()
}
