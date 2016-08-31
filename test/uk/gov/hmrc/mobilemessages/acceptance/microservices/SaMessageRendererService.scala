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

package uk.gov.hmrc.mobilemessages.acceptance.microservices

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import play.api.http.HeaderNames
import uk.gov.hmrc.mobilemessages.domain.Message

class SaMessageRendererService(authToken: String) {

  private val servicePort = 8089

  private lazy val wireMockServer = new WireMockServer(wireMockConfig().port(servicePort))
  private val service = new WireMock(servicePort)

  def start() = {
    wireMockServer.start()
  }

  def stop() = {
    wireMockServer.stop()
  }

  def config = Map(
    "microservice.services.sa-message-renderer.host" -> "localhost",
    "microservice.services.sa-message-renderer.port" -> servicePort
  )

  def successfullyRenders(message: Message): Unit = {
    service.register(get(urlEqualTo(message.renderUrl)).
      withHeader(HeaderNames.AUTHORIZATION, equalTo(authToken)).
      willReturn(aResponse().
        withBody(rendered(message))))
  }

  def rendered(message: Message) = {
    s"""
       |<div>This is a rendered message with id: ${message.id}</div>
     """.stripMargin
  }
}