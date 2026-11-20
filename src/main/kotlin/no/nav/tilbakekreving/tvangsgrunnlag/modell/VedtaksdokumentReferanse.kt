package no.nav.tilbakekreving.tvangsgrunnlag.modell

import kotlinx.serialization.Serializable

@Serializable
data class VedtaksdokumentReferanse(
    val journalpostId: String,
    val dokumentInfoId: String,
)
