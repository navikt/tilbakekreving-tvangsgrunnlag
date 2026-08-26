package no.nav.tilbakekreving.tvangsgrunnlag

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.tvangsgrunnlag.modell.Priority
import no.nav.tilbakekreving.tvangsgrunnlag.modell.Tvangsgrunnlag

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

        get("/tvangsgrunnlag") {
            val tvangsgrunnlag =
                listOf(
                    Tvangsgrunnlag("1", "Tvangsgrunnlag nr 1", Priority.Low),
                    Tvangsgrunnlag("2", "Tvangsgrunnlag nr 2", Priority.Medium),
                    Tvangsgrunnlag("3", "Tvangsgrunnlag nr 3", Priority.High),
                )
            call.respond(tvangsgrunnlag)
        }

        get("/hentDataFraSAF") {
            call.respond(Melding("Hentet data fra SAF"))
        }
    }
}
