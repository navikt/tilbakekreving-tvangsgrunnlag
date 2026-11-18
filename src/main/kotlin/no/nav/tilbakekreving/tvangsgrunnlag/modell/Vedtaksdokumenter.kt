package no.nav.tilbakekreving.tvangsgrunnlag.modell

import kotlinx.serialization.Serializable

/** Referanse til et vedtaksdokument i en journalpost, slik tilbakekreving-backend returnerer det. */
@Serializable
data class VedtaksdokumentReferanse(
    val journalpostId: String,
    val dokumentInfoId: String,
)
