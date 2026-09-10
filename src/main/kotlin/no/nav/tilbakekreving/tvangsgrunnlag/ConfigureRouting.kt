package no.nav.tilbakekreving.tvangsgrunnlag

import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigJoarkClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigSafClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigSkeKravClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigTilbakelosningClient
import no.nav.tilbakekreving.tvangsgrunnlag.modell.TvangsgrunnlagIkkeFunnetException
import no.nav.tilbakekreving.tvangsgrunnlag.modell.TvangsgrunnlagRequest
import no.nav.tilbakekreving.tvangsgrunnlag.modell.UgyldigForespørselException
import no.nav.tilbakekreving.tvangsgrunnlag.tjeneste.TvangsgrunnlagService

@Serializable
data class Melding(
    val melding: String,
)

fun Application.configureRouting(
    tvangsgrunnlagService: TvangsgrunnlagService =
        TvangsgrunnlagService(
            MidlertidigTilbakelosningClient(),
            MidlertidigSkeKravClient(),
            MidlertidigSafClient(),
            MidlertidigJoarkClient(),
        ),
) {
    install(ContentNegotiation) {
        json()
    }

    routing {
        staticResources("static", "static")

        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")

        post("/api/tilbakekreving/tvangsgrunnlag/v1") {
            // TODO: Kall er foreløpig ikke sikret med Maskinporten/JWT-validering. Dette må på
            // plass før tjenesten kan eksponeres for Skatteetaten, jf. maskinporten-oppsettet i
            // .deploy/nais/app-dev.yaml.
            val korrelasjonsid = call.request.header("Korrelasjonsid")
            val klientid = call.request.header("Klientid")
            if (korrelasjonsid.isNullOrBlank() || klientid.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, Melding("Ugyldig forespørsel"))
                return@post
            }

            val request =
                try {
                    call.receive<TvangsgrunnlagRequest>()
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, Melding("Ugyldig forespørsel"))
                    return@post
                }

            try {
                val zip = tvangsgrunnlagService.hentTvangsgrunnlag(request)
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment
                        .withParameter(ContentDisposition.Parameters.FileName, "tvangsgrunnlag.zip")
                        .toString(),
                )
                call.respondBytes(zip, ContentType.Application.Zip)
            } catch (e: UgyldigForespørselException) {
                call.respond(HttpStatusCode.BadRequest, Melding("Ugyldig forespørsel"))
            } catch (e: TvangsgrunnlagIkkeFunnetException) {
                call.respond(HttpStatusCode.NotFound, Melding("Tvangsgrunnlag ikke funnet"))
            }
        }
    }
}
