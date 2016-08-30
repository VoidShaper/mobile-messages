package uk.gov.hmrc.mobilemessages.controllers.model

import com.ning.http.util.Base64
import org.joda.time.{DateTime, DateTimeZone}
import uk.gov.hmrc.crypto._
import uk.gov.hmrc.mobilemessages.acceptance.microservices.Message
import uk.gov.hmrc.mobilemessages.domain.MessageHeader
import uk.gov.hmrc.play.test.UnitSpec

class GetMessagesResponseItemSpec extends UnitSpec {

  val message = new Message("authToken")

  "get messages response" should {
    "be correctly converted from message headers" in {
      val encrypter = new AesCrypto {
        override protected val encryptionKey: String = "hwdODU8hulPkolIryPRkVW=="
      }
      val messageHeader1 = message.headerWith(id = "id1")
      val messageHeader2 = message.headerWith(id = "id2", readTime = Some(DateTime.now(DateTimeZone.UTC)))

      GetMessagesResponseItem.fromAll(Seq(messageHeader1, messageHeader2))(encrypter) shouldBe Seq(
        getMessageResponseItemFor(messageHeader1)(encrypter),
        getMessageResponseItemFor(messageHeader2)(encrypter)
      )
    }
  }

  def getMessageResponseItemFor(messageHeader: MessageHeader)(encrypter: Encrypter): GetMessagesResponseItem = {
    GetMessagesResponseItem(
      messageHeader.id,
      messageHeader.subject,
      messageHeader.validFrom,
      messageHeader.readTime,
      Base64.encode(encrypter.encrypt(PlainText(messageHeader.id)).value.getBytes),
      messageHeader.sentInError
    )
  }
}
