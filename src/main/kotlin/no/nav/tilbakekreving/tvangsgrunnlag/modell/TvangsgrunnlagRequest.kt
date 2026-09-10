package no.nav.tilbakekreving.tvangsgrunnlag.modell

import kotlinx.serialization.Serializable

/**
 * Forespørsel om tvangsgrunnlag, jf. TvangsgrunnlagRequest i
 * https://navikt.github.io/tilbakekreving-api/openapi.yaml
 */
@Serializable
data class TvangsgrunnlagRequest(
    val skyldner: String,
    val oppdragsgiversKravidentifikator: String,
    val skatteetatensKravidentifikator: String,
    val fraOgMedDato: String? = null,
)
