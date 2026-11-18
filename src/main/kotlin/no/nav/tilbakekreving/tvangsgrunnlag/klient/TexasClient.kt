package no.nav.tilbakekreving.tvangsgrunnlag.klient

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class TexasClient(
    private val httpClient: HttpClient,
    private val tokenEndpoint: String,
) {
    suspend fun hentMaskintoken(scope: String): String {
        val response =
            httpClient
                .submitForm(
                    url = tokenEndpoint,
                    formParameters =
                        Parameters.build {
                            append("identity_provider", "entra_id")
                            append("target", scope)
                        },
                ) {
                    expectSuccess = true
                }.body<TokenResponse>()
        return response.accessToken
    }

    @Serializable
    private data class TokenResponse(
        @SerialName("access_token")
        val accessToken: String,
    )
}
