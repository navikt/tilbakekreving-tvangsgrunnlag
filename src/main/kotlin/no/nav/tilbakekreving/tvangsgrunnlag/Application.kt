package no.nav.tilbakekreving.tvangsgrunnlag

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import kotlinx.serialization.json.Json
import no.nav.tilbakekreving.tvangsgrunnlag.klient.HttpTilbakekrevingBackendClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigJoarkClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigSafClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigSkeKravClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigTilbakelosningClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.TexasClient
import no.nav.tilbakekreving.tvangsgrunnlag.tjeneste.TvangsgrunnlagServiceImpl
import no.nav.tilbakekreving.tvangsgrunnlag.tjeneste.UtleveringsStatistikk

fun Application.module() {
    val config = environment.config
    val baseUrl = config.property("tilbakekrevingBackend.baseUrl").getString()
    val scope = config.property("tilbakekrevingBackend.scope").getString()
    val tokenEndpoint = config.property("texas.tokenEndpoint").getString()
    val httpClient =
        HttpClient(CIO) {
            followRedirects = false
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 10_000
                connectTimeoutMillis = 5_000
                socketTimeoutMillis = 10_000
            }
        }
    monitor.subscribe(ApplicationStopped) { httpClient.close() }

    val texasClient = TexasClient(httpClient, tokenEndpoint)
    val prometheusRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    configureRouting(
        prometheusRegistry = prometheusRegistry,
        tvangsgrunnlagService =
            TvangsgrunnlagServiceImpl(
                HttpTilbakekrevingBackendClient(httpClient, baseUrl) { texasClient.hentMaskintoken(scope) },
                MidlertidigTilbakelosningClient(),
                MidlertidigSkeKravClient(),
                MidlertidigSafClient(),
                MidlertidigJoarkClient(),
                UtleveringsStatistikk(prometheusRegistry),
            ),
    )
}
