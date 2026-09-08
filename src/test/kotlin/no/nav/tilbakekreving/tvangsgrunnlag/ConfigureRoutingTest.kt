package no.nav.tilbakekreving.tvangsgrunnlag

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import no.nav.security.mock.oauth2.MockOAuth2Server
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val AUTH_HEADER_NAVN = "Authorization"
private const val AUTH_SCHEME = "Bearer"
private const val KLIENT_ID = "tilbakekreving-tvangsgrunnlag"

class ConfigureRoutingTest {
    private val mockOAuth2Server = MockOAuth2Server()

    @BeforeTest
    fun start() {
        mockOAuth2Server.start()
    }

    @AfterTest
    fun stop() {
        mockOAuth2Server.shutdown()
    }

    /**
     * Overstyrer applikasjonens config direkte i testen i stedet for å stole på
     * miljøvariabler (${?MASKINPORTEN_WELL_KNOWN_URL}) - de settes normalt av Nais
     * i drift, men plukkes ikke opp via System.setProperty i tester.
     */
    private fun ApplicationTestBuilder.settOppTestApp() {
        environment {
            config =
                MapApplicationConfig(
                    "no.nav.security.jwt.issuers.size" to "1",
                    "no.nav.security.jwt.issuers.0.issuer_name" to MASKINPORTEN,
                    "no.nav.security.jwt.issuers.0.discoveryurl" to mockOAuth2Server.wellKnownUrl(MASKINPORTEN).toString(),
                    "no.nav.security.jwt.issuers.0.accepted_audience" to KLIENT_ID,
                )
        }
        application {
            configureAuthentication()
            configureRouting()
        }
    }

    private fun lagTestToken(scope: String): String {
        val claims = mapOf("scope" to scope)
        val jwt =
            mockOAuth2Server.issueToken(
                issuerId = MASKINPORTEN,
                subject = "974761076",
                audience = KLIENT_ID,
                claims = claims,
            )
        return jwt.serialize()
    }

    private fun bearerHeaderVerdi(scope: String): String {
        val token = lagTestToken(scope)
        val deler = listOf(AUTH_SCHEME, token)
        return deler.joinToString(separator = " ")
    }

    @Test
    fun `GET tvangsgrunnlag uten token gir 401`() =
        testApplication {
            settOppTestApp()

            val response = client.get("/tvangsgrunnlag")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `GET tvangsgrunnlag med token uten riktig scope gir 401`() =
        testApplication {
            settOppTestApp()

            val feilScopeHeader = bearerHeaderVerdi(scope = "annet/scope")
            val response =
                client.get("/tvangsgrunnlag") {
                    header(AUTH_HEADER_NAVN, feilScopeHeader)
                }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `GET tvangsgrunnlag med gyldig maskinporten-token returns 200 med zip av 3 dummy-pdfer`() =
        testApplication {
            settOppTestApp()

            val gyldigHeader = bearerHeaderVerdi(scope = PAKREVD_SCOPE)
            val response =
                client.get("/tvangsgrunnlag") {
                    header(AUTH_HEADER_NAVN, gyldigHeader)
                }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType("application", "zip"), response.contentType()?.withoutParameters())

            val zipFilnavn = mutableListOf<String>()
            ZipInputStream(ByteArrayInputStream(response.bodyAsBytes())).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    zipFilnavn.add(entry.name)
                    val pdfBytes = zip.readBytes()
                    // "%PDF" er magic bytes som identifiserer en gyldig PDF-fil.
                    assertTrue(pdfBytes.decodeToString(0, 4) == "%PDF", "Forventet gyldig PDF-innhold i ${entry.name}")
                    entry = zip.nextEntry
                }
            }

            assertEquals(
                listOf("tvangsgrunnlag-1.pdf", "tvangsgrunnlag-2.pdf", "tvangsgrunnlag-3.pdf"),
                zipFilnavn,
            )
        }

    @Test
    fun `GET hentDataFraSAF returns 200 med json-melding uten token`() =
        testApplication {
            settOppTestApp()

            val response = client.get("/hentDataFraSAF")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Application.Json, response.contentType()?.withoutParameters())

            val melding = Json.decodeFromString<Melding>(response.bodyAsText())
            assertEquals(Melding("Hentet data fra SAF"), melding)
        }
}
