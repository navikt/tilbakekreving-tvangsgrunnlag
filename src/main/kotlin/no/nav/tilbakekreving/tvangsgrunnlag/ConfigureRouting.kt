package no.nav.tilbakekreving.tvangsgrunnlag

import io.konform.validation.Invalid
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.http.content.staticResources
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.ktor.v3_0.KtorServerTelemetry
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigJoarkClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigSafClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigSkeKravClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigTilbakelosningClient
import no.nav.tilbakekreving.tvangsgrunnlag.modell.TvangsgrunnlagRequest
import no.nav.tilbakekreving.tvangsgrunnlag.modell.tvangsgrunnlagRequestValidation
import no.nav.tilbakekreving.tvangsgrunnlag.tjeneste.TvangsgrunnlagService
import no.nav.tilbakekreving.tvangsgrunnlag.tjeneste.UtleveringsStatistikk

@Serializable
data class Melding(
    val melding: String,
)

fun Application.configureRouting(
    prometheusRegistry: PrometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT),
    openTelemetry: OpenTelemetry = konfigurerOpenTelemetry(),
    tvangsgrunnlagService: TvangsgrunnlagService =
        TvangsgrunnlagService(
            MidlertidigTilbakelosningClient(),
            MidlertidigSkeKravClient(),
            MidlertidigSafClient(),
            MidlertidigJoarkClient(),
            UtleveringsStatistikk(prometheusRegistry),
        ),
) {
    install(ContentNegotiation) {
        json()
    }
    install(MicrometerMetrics) {
        registry = prometheusRegistry
    }
    install(RequestValidation) {
        validate<TvangsgrunnlagRequest> { request ->
            val resultat = tvangsgrunnlagRequestValidation(request)
            if (resultat is Invalid) {
                ValidationResult.Invalid(resultat.errors.map { it.message })
            } else {
                ValidationResult.Valid
            }
        }
    }
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, Melding("Ugyldig forespørsel: ${cause.reasons.joinToString(", ")}"))
        }
    }
    // Trekker automatisk ut sporingskontekst fra `traceparent`-headeren (W3C Trace Context) på
    // innkommende kall, og starter/kobler på spans deretter. Mangler headeren, starter en ny
    // trace - kallet avvises ikke, siden det ikke er en del av request-kontrakten.
    install(KtorServerTelemetry) {
        setOpenTelemetry(openTelemetry)
    }

    routing {
        staticResources("static", "static")

        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")

        get("/metrics") {
            call.respondText(prometheusRegistry.scrape())
        }

        post("/api/tilbakekreving/tvangsgrunnlag/v1") {
            // TODO: Kall er foreløpig ikke sikret med Maskinporten/JWT-validering. Dette må på
            // plass før tjenesten kan eksponeres for Skatteetaten, jf. maskinporten-oppsettet i
            // .deploy/nais/app-dev.yaml.
            val request = call.receive<TvangsgrunnlagRequest>()

            val zip = tvangsgrunnlagService.hentTvangsgrunnlag(request)
            if (zip == null) {
                call.respond(HttpStatusCode.NotFound, Melding("Tvangsgrunnlag ikke funnet"))
                return@post
            }
            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment
                    .withParameter(ContentDisposition.Parameters.FileName, "tvangsgrunnlag.zip")
                    .toString(),
            )
            call.respondBytes(zip, ContentType.Application.Zip)
        }
    }
}
