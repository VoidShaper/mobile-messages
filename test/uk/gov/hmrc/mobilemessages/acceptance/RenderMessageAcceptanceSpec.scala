/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.mobilemessages.acceptance

import org.joda.time.{DateTime, LocalDate}
import play.api.Play
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.mobilemessages.connector.model.MessageResponseBody
import uk.gov.hmrc.mobilemessages.domain.{Message, MessageId}
import uk.gov.hmrc.mobilemessages.utils.EncryptionUtils
import uk.gov.hmrc.mobilemessages.utils.EncryptionUtils.encrypted

import scala.concurrent.Future

class RenderMessageAcceptanceSpec extends AcceptanceSpec {

  "microservice render message" should {

    "return a rendered message after calling get message and renderer" in new Setup {
      auth.containsUserWith(utr)

      private val messageBody = message.bodyWith(id = messageId1)

      message.getByIdReturns(messageBody)
      saMessageRenderer.successfullyRenders(messageFrom(messageBody))

      val readMessageResponse: Result = messageController.read(None)(
        mobileMessagesGetRequest.withBody(Json.parse(s""" { "url": "${encrypted(messageBody.id)}" } """))
      ).futureValue

      jsonBodyOf(readMessageResponse) shouldBe saMessageRenderer.rendered(messageFrom(messageBody))
    }
  }


  trait Setup {
    val validFromDate = new LocalDate(29348L)
    val readTime = new DateTime(82347L)
    val messageId1 = "messageId90342"
    val messageId2 = "messageId932847"

    val expectedRenderResponse =
      Json.parse(
        s"""
           |[
           |  {
           |    "id": "$messageId1",
           |    "subject": "message subject",
           |    "validFrom": "${validFromDate.toString()}",
           |    "readTimeUrl": "${encrypted(messageId1, configBasedCryptor)}",
           |    "sentInError": false
           |  },
           |  {
           |    "id": "$messageId2",
           |    "subject": "message subject",
           |    "validFrom": "${validFromDate.toString()}",
           |    "readTimeUrl": "${encrypted(messageId2, configBasedCryptor)}",
           |    "readTime": ${readTime.getMillis()},
           |    "sentInError": false
           |  }
           |]
             """.stripMargin)

  }

  def fullUrlFor(serviceName: String, path: String) = {
    val port = Play.current.configuration.getString(s"${microserviceConfigPathFor(serviceName)}.port")
    val host = Play.current.configuration.getString(s"${microserviceConfigPathFor(serviceName)}.host")
    s"http://$host:$port"
  }

  def messageFrom(messageBody: MessageResponseBody): Message = {
    Message(
      MessageId(messageBody.id),
      fullUrlFor(messageBody.renderUrl.service, messageBody.renderUrl.url),
      None
    )
  }
}
