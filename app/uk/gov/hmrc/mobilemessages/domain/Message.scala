package uk.gov.hmrc.mobilemessages.domain

final case class MessageId(value: String)

final case class Message(id: MessageId, renderUrl: String, markAsReadUrl: Option[String])
