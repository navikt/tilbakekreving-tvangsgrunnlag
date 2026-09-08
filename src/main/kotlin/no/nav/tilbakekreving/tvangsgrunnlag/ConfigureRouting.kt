package no.nav.tilbakekreving.tvangsgrunnlag

import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.tvangsgrunnlag.modell.Priority
import no.nav.tilbakekreving.tvangsgrunnlag.modell.Tvangsgrunnlag
import no.nav.tilbakekreving.tvangsgrunnlag.pdf.genererTvangsgrunnlagZip

@Serializable
data class Melding(
    val melding: String,
)

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        staticResources("static", "static")

        authenticate(MASKINPORTEN) {
            get("/tvangsgrunnlag") {
                val tvangsgrunnlag =
                    listOf(
                        Tvangsgrunnlag("1", "Tvangsgrunnlag nr 1", Priority.Low),
                        Tvangsgrunnlag("2", "Tvangsgrunnlag nr 2", Priority.Medium),
                        Tvangsgrunnlag("3", "Tvangsgrunnlag nr 3", Priority.High),
                    )
                // Simulerer PDF-dokumenter som senere skal hentes fra et fagsystem -
                // returnerer i mellomtiden en zip med dummy-PDF-er, én per tvangsgrunnlag.
                val zipBytes = genererTvangsgrunnlagZip(tvangsgrunnlag)
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment
                        .withParameter(ContentDisposition.Parameters.FileName, "tvangsgrunnlag.zip")
                        .toString(),
                )
                call.respondBytes(zipBytes, contentType = ContentType("application", "zip"))
            }
        }

        get("/hentDataFraSAF") {
            call.respond(Melding("Hentet data fra SAF"))
        }
    }
}
