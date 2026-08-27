package no.nav.tilbakekreving.tvangsgrunnlag

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import no.nav.tilbakekreving.tvangsgrunnlag.modell.Priority
import no.nav.tilbakekreving.tvangsgrunnlag.modell.Tvangsgrunnlag
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigureRoutingTest {

    @Test
    fun `GET tvangsgrunnlag returns 200 med json-liste`() = testApplication {
        application { configureRouting() }

        val response = client.get("/tvangsgrunnlag")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ContentType.Application.Json, response.contentType()?.withoutParameters())

        val tvangsgrunnlag = Json.decodeFromString<List<Tvangsgrunnlag>>(response.bodyAsText())
        assertEquals(
            listOf(
                Tvangsgrunnlag("1", "Tvangsgrunnlag nr 1", Priority.Low),
                Tvangsgrunnlag("2", "Tvangsgrunnlag nr 2", Priority.Medium),
                Tvangsgrunnlag("3", "Tvangsgrunnlag nr 3", Priority.High),
            ),
            tvangsgrunnlag,
        )
    }

    @Test
    fun `GET hentDataFraTilbakekreving returns 200 med json-melding`() = testApplication {
        application { configureRouting() }

        val response = client.get("/hentDataFraTilbakekreving")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ContentType.Application.Json, response.contentType()?.withoutParameters())

        val melding = Json.decodeFromString<Melding>(response.bodyAsText())
        assertEquals(Melding("Hentet data fra tilbakekreving"), melding)
    }
}
