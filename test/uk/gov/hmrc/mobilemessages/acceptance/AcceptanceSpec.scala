package uk.gov.hmrc.mobilemessages.acceptance

import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import play.api.test.{FakeApplication, FakeRequest}
import play.api.{GlobalSettings, Play}
import uk.gov.hmrc.crypto.CryptoWithKeysFromConfig
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.mobilemessages.acceptance.microservices.{AuthService, MessageService, SaMessageRendererService}
import uk.gov.hmrc.mobilemessages.acceptance.utils.WiremockServiceLocatorSugar
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.mobilemessages.controllers.{LiveMobileMessagesController, MobileMessagesController}

trait AcceptanceSpec extends UnitSpec
  with MockitoSugar
  with ScalaFutures
  with WiremockServiceLocatorSugar
  with BeforeAndAfter {

  before {
    startMockServer()
    saMessageRenderer.start()
    Play.start(app)
  }

  after {
    Play.stop()
    stopMockServer()
    saMessageRenderer.stop()
  }

  val messageController: MobileMessagesController = LiveMobileMessagesController

  val utr = SaUtr("109238")

  val auth = new AuthService
  val message = new MessageService(auth.token)
  val saMessageRenderer = new SaMessageRendererService(auth.token)
  val configBasedCryptor = CryptoWithKeysFromConfig(baseConfigKey = "queryParameter.encryption")

  val mobileMessagesGetRequest = FakeRequest("GET", "/").
    withHeaders(
      ("Accept", "application/vnd.hmrc.1.0+json"),
      ("Authorization", auth.token)
    )

  object TestGlobal extends GlobalSettings

  def microserviceConfigPathFor(serviceName: String) = {
    s"microservice.services.$serviceName"
  }

  implicit val app = FakeApplication(
    withGlobal = Some(TestGlobal),
    additionalConfiguration = Map(
      "appName" -> "application-name",
      "appUrl" -> "http://microservice-name.service",
      s"${microserviceConfigPathFor("auth")}.host" -> stubHost,
      s"${microserviceConfigPathFor("auth")}.port" -> stubPort,
      s"${microserviceConfigPathFor("message")}.host" -> stubHost,
      s"${microserviceConfigPathFor("message")}.port" -> stubPort,
      "auditing.enabled" -> "false",
      "queryParameter.encryption.key" -> "kepODU8hulPkolIryPOrTY=="
    ) ++ saMessageRenderer.config
  )

}
