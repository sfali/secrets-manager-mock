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
  private val dbFile: String = "target/test.db"
  private val secretData = SecretModel("app_adm", "Example123")
  private val defaultVersionId = toVersionId(defaultName)
  private val repository = NitriteRepository(new DBSettings {
    override val filePath: String = dbFile
    override val userName: String = "root"
    override val password: String = "Example123"
  })

  override protected def afterAll(): Unit = {
    super.afterAll()
    repository.close()
    Files.deleteIfExists(Paths.get(dbFile))
  }

  it should "insert new secret" in {
    val description = "new test secret"
    val versionId = defaultVersionId
    val response = repository.createSecret(name = defaultName,
      description = Some(description),
      versionId = Some(versionId),
      secretString = Some(secretData.toJson))
    response mustBe SecretResponse(response.arn, defaultName, Some(versionId), CurrentLabel :: Nil)
  }

  it should "reject attempt to create duplicate secret" in {
    val secretString = secretData.copy(password = "Example567").toJson
    an[ResourceExistsException] must be thrownBy
      repository.createSecret(name = defaultName, versionId = Some(defaultVersionId), secretString = Some(secretString))
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
    version.versionId mustEqual defaultVersionId
    version.secretString.get mustEqual secretData.toJson
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
    version.versionId mustEqual defaultVersionId
    version.secretString.get mustEqual secretData.toJson
    version.stages must have length 1
    val stage = version.stages.head
    stage mustEqual CurrentLabel
  }

  it should "get secret with name and versionId" in {
    val secretEntity = repository.getSecret(defaultName, versionId = Some(defaultVersionId))
    secretEntity.arn must include(defaultName)
    secretEntity.name mustEqual defaultName
    secretEntity.description mustBe Some("new test secret")
    secretEntity.kmsKeyId mustEqual DefaultKmsKeyId
    val versions = secretEntity.versions
    versions must have length 1
    val version = versions.head
    version.versionId mustEqual defaultVersionId
    version.secretString.get mustEqual secretData.toJson
    version.stages must have length 1
    val stage = version.stages.head
    stage mustEqual CurrentLabel
  }

  it should "not find non-existing version" in {
    an[ResourceNotFoundException] must be thrownBy
      repository.getSecret(defaultName, versionId = Some(UUID.randomUUID().toString))
  }

  it should "fail attempt to update existing secret value" in {
    val secretData = this.secretData.copy(password = "Example124")
    an[ResourceExistsException] must be thrownBy
      repository.putSecretValue(defaultName, defaultVersionId, secretString = Some(secretData.toJson))
  }

  it should "add new staging label" in {
    val secretData = this.secretData.copy(password = "Example124")
    val versionId = toVersionId(PreviousLabel)
    repository.putSecretValue(defaultName, versionId, secretString = Some(secretData.toJson),
      versionStages = PreviousLabel :: Nil)
    val secretEntity = repository.describeSecret(defaultName)
    val versions = secretEntity.versions

    val currentVersion = versions.filter(_.versionId == defaultVersionId).head
    currentVersion.versionId mustEqual defaultVersionId
    currentVersion.secretString.get mustEqual this.secretData.toJson

    val previousVersion = versions.filter(_.versionId == versionId).head
    previousVersion.versionId mustEqual versionId
    previousVersion.secretString.get mustEqual secretData.toJson
  }

  it should "add new current label and move current to previous" in {
    val secretData = this.secretData.copy(password = "Example567")
    val versionId = toVersionId("NewCurrent")
    repository.putSecretValue(defaultName, versionId, secretString = Some(secretData.toJson))
    val secretEntity = repository.describeSecret(defaultName)
    val versions = secretEntity.versions

    val currentVersion = versions.filter(_.versionId == versionId).head
    currentVersion.versionId mustEqual versionId
    currentVersion.secretString.get mustEqual secretData.toJson

    val previousVersion = versions.filter(_.versionId == defaultVersionId).head
    previousVersion.versionId mustEqual defaultVersionId
    previousVersion.secretString.get mustEqual this.secretData.toJson

    val oldPreviousVersion = versions.filter(_.versionId == toVersionId(PreviousLabel)).head
    oldPreviousVersion.stages mustBe empty
  }

  private lazy val toVersionId = (name: String) => UUID.nameUUIDFromBytes(name.getBytes).toString
}
