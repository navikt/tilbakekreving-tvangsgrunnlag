package no.nav.tilbakekreving.tvangsgrunnlag.modell

import java.time.LocalDate

/**
 * Referanse til et tilbakekrevingsdokument (vedtak eller endringsvedtak) i SAF.
 * `journalpostId` og `dokumentInfoId` identifiserer dokumentet i SAF, mens `sendtDato`
 * brukes til å filtrere bort dokumenter som er eldre enn `fraOgMedDato` i forespørselen.
 */
data class DokumentReferanse(
    val journalpostId: String,
    val dokumentInfoId: String,
    val sendtDato: LocalDate,
)
