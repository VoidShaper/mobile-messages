package uk.gov.hmrc.mobilemessages.connector.model

case class ResourceActionLocation(service: String, url: String)

case class MessageResponseBody(id: String,
                               renderUrl: ResourceActionLocation,
                               markAsReadUrl: Option[ResourceActionLocation])

