package no.nav.tilbakekreving.tvangsgrunnlag.klient

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.ContentType
import no.nav.tilbakekreving.tvangsgrunnlag.modell.JournalpostReferanse

interface FamilieTilbakeClient {
    suspend fun hentJournalposter(vedtakId: String): List<JournalpostReferanse>
}

class HttpFamilieTilbakeClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val hentAccessToken: suspend () -> String,
) : FamilieTilbakeClient {
    override suspend fun hentJournalposter(vedtakId: String): List<JournalpostReferanse> =
        httpClient
            .get("${baseUrl.trimEnd('/')}/api/v1/dokumenter/vedtak/$vedtakId") {
                expectSuccess = true
                bearerAuth(hentAccessToken())
                accept(ContentType.Application.Json)
            }.body()
}
