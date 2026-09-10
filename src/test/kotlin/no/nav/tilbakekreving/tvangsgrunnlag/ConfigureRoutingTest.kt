package no.nav.tilbakekreving.tvangsgrunnlag

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigJoarkClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigSafClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigSkeKravClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigTilbakelosningClient
import no.nav.tilbakekreving.tvangsgrunnlag.tjeneste.TvangsgrunnlagService
import java.util.zip.ZipInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfigureRoutingTest {
    private val kjentSkyldner = "12345678901"
    private val kjentOppdragsgiversKravidentifikator = "NAV-KRAV-2024-001"
    private val kjentSkatteetatensKravidentifikator = "550e8400-e29b-41d4-a716-446655440000"

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

    @Test
    fun `POST tvangsgrunnlag returner 200 og zip for kjent krav`() =
        testApplication {
            application {
                configureRouting(testService())
            }

            val response =
                client.post("/api/tilbakekreving/tvangsgrunnlag/v1") {
                    header("Korrelasjonsid", "11111111-1111-1111-1111-111111111111")
                    header("Klientid", "skatteetaten-klient")
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
    fun `POST tvangsgrunnlag returner 400 for manglende header`() =
        testApplication {
            application {
                configureRouting(testService())
            }

            val response =
                client.post("/api/tilbakekreving/tvangsgrunnlag/v1") {
                    contentType(ContentType.Application.Json)
                    setBody(kravRequestJson())
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `POST tvangsgrunnlag returner 400 for ugyldig body`() =
        testApplication {
            application {
                configureRouting(testService())
            }

            val response =
                client.post("/api/tilbakekreving/tvangsgrunnlag/v1") {
                    header("Korrelasjonsid", "11111111-1111-1111-1111-111111111111")
                    header("Klientid", "skatteetaten-klient")
                    contentType(ContentType.Application.Json)
                    setBody("{}")
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `POST tvangsgrunnlag returner 404 for ukjent krav`() =
        testApplication {
            application {
                configureRouting(testService())
            }

            val response =
                client.post("/api/tilbakekreving/tvangsgrunnlag/v1") {
                    header("Korrelasjonsid", "11111111-1111-1111-1111-111111111111")
                    header("Klientid", "skatteetaten-klient")
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

    @Test
    fun `GET hentDataFraSAF returns 200 med json-melding`() =
        testApplication {
            application {
                configureRouting(testService())
            }

            val response = client.get("/hentDataFraSAF")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Application.Json, response.contentType()?.withoutParameters())

            val melding = Json.decodeFromString<Melding>(response.bodyAsText())
            assertEquals(Melding("Hentet data fra SAF"), melding)
        }
}
