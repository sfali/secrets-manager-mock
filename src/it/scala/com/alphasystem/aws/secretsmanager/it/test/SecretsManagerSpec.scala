package com.alphasystem.aws.secretsmanager.it.test

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import akka.testkit.TestKit
import akka.util.Timeout
import com.alphasystem.aws.secretsmanager._
import com.alphasystem.aws.secretsmanager.it._
import com.alphasystem.aws.secretsmanager.repository.NitriteRepository
import com.alphasystem.aws.secretsmanager.test._
import com.alphasystem.aws.secretsmanager.{SecretsManagerServer, Settings}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, MustMatchers}
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient
import software.amazon.awssdk.services.secretsmanager.model.{CreateSecretRequest, DeleteSecretRequest}
import scala.concurrent.duration._

import scala.compat.java8.FutureConverters._

class SecretsManagerSpec
  extends TestKit(ActorSystem("it-system"))
    with FlatSpecLike
    with BeforeAndAfterAll
    with MustMatchers
    with ScalaFutures {

  private implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(15, Seconds),
    interval = Span(500, Millis))
  private implicit val timeout: Timeout = Timeout(20.seconds)

  private implicit val mat: Materializer = ActorMaterializer()
  private implicit val settings: Settings = Settings()
  private implicit val repository: NitriteRepository = NitriteRepository()

  private val defaultName = "local/test"
  private val secretData = SecretModel("app_adm", "Example123")

  private val client =
    SecretsManagerAsyncClient
      .builder()
      .endpointOverride(settings.secretsManager.endPoint)
      .region(settings.awsSettings.region)
      .credentialsProvider(settings.awsSettings.credentialsProvider)
      .build()

  private val server = SecretsManagerServer()

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    server.start()
  }

  it should "create new secret with given secret" in {
    val versionId = toVersionId(defaultName)
    val request = CreateSecretRequest
      .builder()
      .name(defaultName)
      .description("test description")
      .clientRequestToken(versionId)
      .secretString(secretData.toJson)
      .build()

    val response = client.createSecret(request).toScala.futureValue
    getNameFromSecretId(response.arn()) mustEqual defaultName
    response.name() mustEqual defaultName
    response.versionId() mustEqual versionId
  }

  it should "delete default secret" in {
    val request = DeleteSecretRequest
      .builder()
      .secretId(defaultName)
      .forceDeleteWithoutRecovery(true)
      .build()

    val response = client.deleteSecret(request).toScala.futureValue
    getNameFromSecretId(response.arn()) mustEqual defaultName
    response.name() mustEqual defaultName
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    client.close()
    server.shutdown()
  }
}
