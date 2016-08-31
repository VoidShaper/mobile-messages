package uk.gov.hmrc.mobilemessages.acceptance.microservices

import uk.gov.hmrc.domain.SaUtr
import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.libs.json.Json

class AuthService {
  def token = "authToken9349872"

  def containsUserWith(utr: SaUtr) = {
    givenThat(get(urlMatching("/auth/authority*"))
      .willReturn(aResponse().withStatus(200).withBody(
        Json.parse(
          s"""
             | {
             |    "confidenceLevel": 500,
             |    "uri": "testUri",
             |    "accounts": {
             |        "sa": {
             |            "utr": "$utr"
             |         },
             |         "paye": {
             |            "nino": "BC233445B"
             |         }
             |     }
             | }""".
            stripMargin
        )
          .toString())))
  }
}
