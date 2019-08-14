package com.alphasystem.aws.secretsmanager.model

import java.time.OffsetDateTime

case class Version(versionId: String,
                   secretString: Option[String] = None,
                   secretBinary: Option[String] = None,
                   createdDate: OffsetDateTime = OffsetDateTime.now(),
                   lastAccessedDate: Option[OffsetDateTime] = None,
                   stages: List[String] = Nil)
