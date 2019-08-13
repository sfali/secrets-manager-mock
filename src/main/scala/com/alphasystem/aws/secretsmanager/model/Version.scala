package com.alphasystem.aws.secretsmanager.model

import java.time.OffsetDateTime

case class Version(versionId: String,
                   secret: String,
                   createdDate: OffsetDateTime = OffsetDateTime.now(),
                   lastAccessedDate: Option[OffsetDateTime] = None,
                   stages: List[String] = Nil)
