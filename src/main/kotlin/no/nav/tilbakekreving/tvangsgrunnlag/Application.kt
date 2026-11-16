package no.nav.tilbakekreving.tvangsgrunnlag

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import kotlinx.serialization.json.Json
import no.nav.tilbakekreving.tvangsgrunnlag.klient.HttpFamilieTilbakeClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.TexasClient

fun Application.module() {
    val config = environment.config
    val baseUrl = config.property("familieTilbake.baseUrl").getString()
    val scope = config.property("familieTilbake.scope").getString()
    val tokenEndpoint = config.property("familieTilbake.tokenEndpoint").getString()
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
    configureRouting(
        familieTilbakeClient = HttpFamilieTilbakeClient(httpClient, baseUrl) { texasClient.hentMaskintoken(scope) },
    )
}
