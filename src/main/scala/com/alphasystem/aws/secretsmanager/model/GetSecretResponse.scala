package com.alphasystem.aws.secretsmanager.model

import java.time.OffsetDateTime

case class GetSecretResponse(arn: String,
                             name: String,
                             createDate: OffsetDateTime,
                             versionId: String,
                             secretBinary: Option[String] = None,
                             secretString: Option[String] = None,
                             versionStages: List[String] = Nil)

object GetSecretResponse {
}
