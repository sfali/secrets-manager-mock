package com.alphasystem.aws.secretsmanager.model

case class SecretResponse(arn: String, name: String, versionId: String, versionStages: List[String])
