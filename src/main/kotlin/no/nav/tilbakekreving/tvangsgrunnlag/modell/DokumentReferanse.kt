package no.nav.tilbakekreving.tvangsgrunnlag.modell

import java.time.LocalDate

enum class DokumentType {
    VEDTAK,
    ENDRINGSVEDTAK,
}

/**
 * Referanse til et tilbakekrevingsdokument (vedtak eller endringsvedtak) i tilbakeløsningen.
 * `dato` brukes til å filtrere bort dokumenter som er eldre enn `fraOgMedDato` i forespørselen.
 */
data class DokumentReferanse(
    val dokumentId: String,
    val type: DokumentType,
    val tittel: String,
    val dato: LocalDate,
)
