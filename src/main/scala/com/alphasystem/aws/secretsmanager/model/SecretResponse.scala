package com.alphasystem.aws.secretsmanager.model

case class SecretResponse(arn: String,
                          name: String,
                          versionId: Option[String] = None,
                          versionStages: List[String] = Nil)
