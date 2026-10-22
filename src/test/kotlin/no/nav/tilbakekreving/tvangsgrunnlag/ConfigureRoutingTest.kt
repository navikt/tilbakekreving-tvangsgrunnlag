package no.nav.tilbakekreving.tvangsgrunnlag

import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.util.zip.ZipInputStream
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigJoarkClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigSafClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigSkeKravClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigTilbakelosningClient
import no.nav.tilbakekreving.tvangsgrunnlag.tjeneste.TvangsgrunnlagService

class ConfigureRoutingTest {
    private val mockOAuth2Server = MockOAuth2Server()
    private val kjentSkyldner = "12345678901"
    private val kjentOppdragsgiversKravidentifikator = "NAV-KRAV-2024-001"
    private val kjentSkatteetatensKravidentifikator = "550e8400-e29b-41d4-a716-446655440000"

    @BeforeTest
    fun startMockOAuth2Server() {
        mockOAuth2Server.start()
    }

    @AfterTest
    fun stopMockOAuth2Server() {
        mockOAuth2Server.shutdown()
    }

    private fun kravRequestJson(
        skyldner: String = kjentSkyldner,
        oppdragsgiversKravidentifikator: String = kjentOppdragsgiversKravidentifikator,
        skatteetatensKravidentifikator: String = kjentSkatteetatensKravidentifikator,
        fraOgMedDato: String? = null,
    ): String =
        """
        {
            "skyldner": "$skyldner",
            "oppdragsgiversKravidentifikator": "$oppdragsgiversKravidentifikator",
            "skatteetatensKravidentifikator": "$skatteetatensKravidentifikator"
            ${fraOgMedDato?.let { ""","fraOgMedDato": "$it"""" } ?: ""}
        }
        """.trimIndent()

    private fun testService() =
        TvangsgrunnlagService(
            MidlertidigTilbakelosningClient(),
            MidlertidigSkeKravClient(),
            MidlertidigSafClient(),
            MidlertidigJoarkClient(),
        )

    private fun withAuthenticatedApplication(block: suspend ApplicationTestBuilder.() -> Unit) =
        testApplication {
            environment {
                config = tokenValidationConfig()
            }
            application {
                configureRouting(tvangsgrunnlagService = testService())
            }
            block()
        }

    private fun tokenValidationConfig() =
        MapApplicationConfig().apply {
            put("no.nav.security.jwt.issuers.size", "1")
            put("no.nav.security.jwt.issuers.0.issuer_name", "maskinporten")
            put("no.nav.security.jwt.issuers.0.discoveryurl", mockOAuth2Server.wellKnownUrl("maskinporten").toString())
            put("no.nav.security.jwt.issuers.0.accepted_audience", "test-audience")
        }

    private fun token(scope: String = "nav:tilbakekreving/tvangsgrunnlag.read") =
        mockOAuth2Server
            .issueToken(
                issuerId = "maskinporten",
                subject = "test-consumer",
                audience = "test-audience",
                claims = mapOf("scope" to scope),
            ).serialize()

    @Test
    fun `POST tvangsgrunnlag avviser forespørsel uten token`() =
        withAuthenticatedApplication {
            val response = client.post("/api/tilbakekreving/tvangsgrunnlag/v1")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `POST tvangsgrunnlag avviser ugyldig token`() =
        withAuthenticatedApplication {
            val response =
                client.post("/api/tilbakekreving/tvangsgrunnlag/v1") {
                    bearerAuth("not-a-token")
                }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `POST tvangsgrunnlag avviser token uten påkrevd scope`() =
        withAuthenticatedApplication {
            val response =
                client.post("/api/tilbakekreving/tvangsgrunnlag/v1") {
                    bearerAuth(token(scope = "nav:tilbakekreving/tvangsgrunnlag.write"))
                }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `POST tvangsgrunnlag returner 200 og zip for kjent krav`() =
        withAuthenticatedApplication {
            val response =
                client.post("/api/tilbakekreving/tvangsgrunnlag/v1") {
                    bearerAuth(token())
                    contentType(ContentType.Application.Json)
                    setBody(kravRequestJson())
                }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Application.Zip, response.contentType()?.withoutParameters())

            val antallEntries =
                ZipInputStream(response.bodyAsBytes().inputStream()).use { zip ->
                    generateSequence { zip.nextEntry }.count()
                }
            assertTrue(antallEntries > 0)
        }

    @Test
    fun `POST tvangsgrunnlag returner 400 for tom skyldner`() =
        withAuthenticatedApplication {
            val response =
                client.post("/api/tilbakekreving/tvangsgrunnlag/v1") {
                    bearerAuth(token())
                    contentType(ContentType.Application.Json)
                    setBody(kravRequestJson(skyldner = ""))
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `POST tvangsgrunnlag returner 404 for ukjent krav`() =
        withAuthenticatedApplication {
            val response =
                client.post("/api/tilbakekreving/tvangsgrunnlag/v1") {
                    bearerAuth(token())
                    contentType(ContentType.Application.Json)
                    setBody(
                        kravRequestJson(
                            skyldner = "99999999999",
                            skatteetatensKravidentifikator = "00000000-0000-0000-0000-000000000000",
                        ),
                    )
                }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }
}
