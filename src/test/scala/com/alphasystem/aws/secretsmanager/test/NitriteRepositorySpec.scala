package com.alphasystem.aws.secretsmanager.test

import java.nio.file.{Files, Paths}
import java.util.UUID

import com.alphasystem.aws.secretsmanager._
import com.alphasystem.aws.secretsmanager.model.{Errors, SecretResponse}
import com.alphasystem.aws.secretsmanager.repository.NitriteRepository
import org.scalatest.{BeforeAndAfterAll, FlatSpec, MustMatchers}

class NitriteRepositorySpec
  extends FlatSpec
    with MustMatchers
    with BeforeAndAfterAll {

  import Errors._

  private val defaultName = "local/test"
  private val dbFile: String = "test.db"
  private val secretData = SecretModel("app_adm", "Example123")
  private val repository = NitriteRepository(dbFile)

  override protected def afterAll(): Unit = {
    super.afterAll()
    repository.close()
    Files.deleteIfExists(Paths.get(dbFile))
  }

  it should "insert new secret" in {
    val description = "new test secret"
    val versionId = toVersionId(defaultName)
    val response = repository.createSecret(name = defaultName,
      description = Some(description),
      versionId = versionId,
      secretString = Some(secretData.toJson))
    println(repository.describeSecret(defaultName).versions)
    response mustBe SecretResponse(response.arn, defaultName, versionId, CurrentLabel :: Nil)
  }

  it should "reject attempt to create duplicate secret" in {
    val versionId = UUID.randomUUID().toString
    an[ResourceExistsException.type] must be thrownBy
      repository.createSecret(name = defaultName, versionId = versionId, secretString = Some(secretData.toJson))
  }

  it should "get secret by name without providing stage should return `Current` stage" in {
    val secretEntity = repository.getSecret(defaultName)
    secretEntity.arn must include(defaultName)
    secretEntity.name mustEqual defaultName
    secretEntity.description mustBe Some("new test secret")
    secretEntity.kmsKeyId mustEqual DefaultKmsKeyId
    val versions = secretEntity.versions
    versions must have length 1
    val version = versions.head
    version.versionId mustEqual toVersionId(defaultName)
    version.secret mustEqual secretData.toJson
    version.stages must have length 1
    val stage = version.stages.head
    stage mustEqual CurrentLabel
  }

  it should "get secret by partial arn" in {
    val secretEntity = repository.getSecret(s"$ARNPrefix$defaultName")
    secretEntity.arn must include(defaultName)
    secretEntity.name mustEqual defaultName
    secretEntity.description mustBe Some("new test secret")
    secretEntity.kmsKeyId mustEqual DefaultKmsKeyId
    val versions = secretEntity.versions
    versions must have length 1
    val version = versions.head
    version.versionId mustEqual toVersionId(defaultName)
    version.secret mustEqual secretData.toJson
    version.stages must have length 1
    val stage = version.stages.head
    stage mustEqual CurrentLabel
  }

  it should "get secret with name and versionId" in {
    val secretEntity = repository.getSecret(defaultName, versionId = Some(toVersionId(defaultName)))
    secretEntity.arn must include(defaultName)
    secretEntity.name mustEqual defaultName
    secretEntity.description mustBe Some("new test secret")
    secretEntity.kmsKeyId mustEqual DefaultKmsKeyId
    val versions = secretEntity.versions
    versions must have length 1
    val version = versions.head
    version.versionId mustEqual toVersionId(defaultName)
    version.secret mustEqual secretData.toJson
    version.stages must have length 1
    val stage = version.stages.head
    stage mustEqual CurrentLabel
  }

  it should "not find non-existing version" in {
    an[ResourceNotFoundException.type] must be thrownBy
      repository.getSecret(defaultName, versionId = Some(UUID.randomUUID().toString))
  }

  it should "fail attempt to update existing secret value" in {
    val secretData = this.secretData.copy(password = "Example124")
    an[ResourceExistsException.type] must be thrownBy
      repository.putSecretValue(defaultName, toVersionId(defaultName), secretString = Some(secretData.toJson))
  }

  it should "add new staging label" in {
    val secretData = this.secretData.copy(password = "Example124")
    val versionId = toVersionId("NewLabel")
    repository.putSecretValue(defaultName, versionId, secretString = Some(secretData.toJson),
      versionStages = PreviousLabel :: Nil)
    val secretEntity = repository.describeSecret(defaultName)
    println(secretEntity.versions)
  }

  private lazy val toVersionId = (name: String) => UUID.nameUUIDFromBytes(name.getBytes).toString
}
