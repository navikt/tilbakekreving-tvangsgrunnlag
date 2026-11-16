package no.nav.tilbakekreving.tvangsgrunnlag.modell

import kotlinx.serialization.Serializable

/** Referanse til et journalført vedtaksdokument, slik familie-tilbake returnerer det. */
@Serializable
data class JournalpostReferanse(
    val journalpostId: String,
    val dokumentInfoId: String,
)

private const val OPPDRAGSGIVERS_KRAVIDENTIFIKATOR_PREFIX = "OB04"

/**
 * Utleder vedtakId fra oppdragsgiversKravidentifikator:
 * prefikset "OB04" fjernes, deretter ledende nuller. "OB04000123456" gir "123456".
 * Returnerer null hvis prefikset mangler eller resten ikke er 1–64 ASCII-sifre som passer i Long.
 */
fun utledVedtakId(oppdragsgiversKravidentifikator: String): String? {
    if (!oppdragsgiversKravidentifikator.startsWith(OPPDRAGSGIVERS_KRAVIDENTIFIKATOR_PREFIX)) {
        return null
    }

    val sifre = oppdragsgiversKravidentifikator.removePrefix(OPPDRAGSGIVERS_KRAVIDENTIFIKATOR_PREFIX)
    if (sifre.length !in 1..64 || !sifre.all { it in '0'..'9' }) {
        return null
    }

    val vedtakId = sifre.trimStart('0').ifEmpty { "0" }
    return vedtakId.takeIf { it.toLongOrNull() != null }
}
