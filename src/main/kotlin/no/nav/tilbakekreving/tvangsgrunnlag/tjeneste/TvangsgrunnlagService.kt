package no.nav.tilbakekreving.tvangsgrunnlag.tjeneste

import no.nav.tilbakekreving.tvangsgrunnlag.klient.JoarkClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.SafClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.SkeKravClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.TilbakelosningClient
import no.nav.tilbakekreving.tvangsgrunnlag.modell.DokumentReferanse
import no.nav.tilbakekreving.tvangsgrunnlag.modell.TvangsgrunnlagRequest
import no.nav.tilbakekreving.tvangsgrunnlag.modell.UgyldigForespørselException
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Orkestrerer hele hent-tvangsgrunnlag-flyten, jf. beskrivelsen i
 * https://navikt.github.io/tilbakekreving-api/openapi.yaml
 */
class TvangsgrunnlagService(
    private val tilbakelosningClient: TilbakelosningClient,
    private val skeKravClient: SkeKravClient,
    private val safClient: SafClient,
    private val joarkClient: JoarkClient,
    private val statistikk: UtleveringsStatistikk = UtleveringsStatistikk(),
) {
    private val logger = LoggerFactory.getLogger(TvangsgrunnlagService::class.java)

    /** Returnerer null dersom tvangsgrunnlaget ikke finnes (eller ikke har dokumenter etter fraOgMedDato). */
    fun hentTvangsgrunnlag(request: TvangsgrunnlagRequest): ByteArray? {
        val fraOgMedDato = validerOgHentFraOgMedDato(request)

        val alleDokumenter =
            tilbakelosningClient.hentDokumenter(
                request.skyldner,
                request.oppdragsgiversKravidentifikator,
                request.skatteetatensKravidentifikator,
            )
        val dokumenterEtterDato =
            alleDokumenter.filter { fraOgMedDato == null || !it.sendtDato.isBefore(fraOgMedDato) }

        if (dokumenterEtterDato.isEmpty()) {
            val registrertISkeKrav = skeKravClient.finnesKravidentifikator(request.skatteetatensKravidentifikator)
            loggIkkeFunnet(request, registrertISkeKrav)
            return null
        }

        val zip = zipDokumenter(dokumenterEtterDato)

        joarkClient.registrerUtlevering(request.skyldner, dokumenterEtterDato)

        // TODO: Audit-logg henvendelsen og de utleverte dokumentene iht. Navs krav til
        // sporingslogg (auditlogger/CEF-format). Loggen bør inneholde hvem som spurte
        // (Klientid/Korrelasjonsid), hvilket krav/skyldner det gjaldt og hvilke dokumenter som
        // ble utlevert. Avklar med sikkerhet/arkitektur hvilken audit-logg-kanal som skal brukes.

        dokumenterEtterDato.forEach { statistikk.registrerUtlevering("${it.journalpostId}-${it.dokumentInfoId}") }

        return zip
    }

    private fun validerOgHentFraOgMedDato(request: TvangsgrunnlagRequest): LocalDate? {
        if (request.skyldner.isBlank() ||
            request.oppdragsgiversKravidentifikator.isBlank() ||
            request.skatteetatensKravidentifikator.isBlank()
        ) {
            throw UgyldigForespørselException(
                "skyldner, oppdragsgiversKravidentifikator og skatteetatensKravidentifikator er påkrevd",
            )
        }

        runCatching { UUID.fromString(request.skatteetatensKravidentifikator) }
            .getOrElse { throw UgyldigForespørselException("skatteetatensKravidentifikator må være en gyldig UUID") }

        val fraOgMedDato = request.fraOgMedDato
        if (fraOgMedDato.isNullOrBlank()) {
            return null
        }
        return try {
            LocalDate.parse(fraOgMedDato)
        } catch (e: DateTimeParseException) {
            throw UgyldigForespørselException("fraOgMedDato må være på formatet yyyy-MM-dd")
        }
    }

    private fun zipDokumenter(dokumenter: List<DokumentReferanse>): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        ZipOutputStream(byteArrayOutputStream).use { zipOutputStream ->
            dokumenter.forEach { dokument ->
                val pdfBytes = safClient.hentPdf(dokument)
                zipOutputStream.putNextEntry(ZipEntry("${dokument.journalpostId}-${dokument.dokumentInfoId}.pdf"))
                zipOutputStream.write(pdfBytes)
                zipOutputStream.closeEntry()
            }
        }
        return byteArrayOutputStream.toByteArray()
    }

    private fun loggIkkeFunnet(
        request: TvangsgrunnlagRequest,
        registrertISkeKrav: Boolean,
    ) {
        // NB: Logger bevisst hvilke kravidentifikatorer/skyldner SKE spurte om, jf. krav i punkt 2.
        // TODO: skyldner kan være et fødselsnummer/organisasjonsnummer (PII) - avklar om dette
        // feltet må maskeres i loggen, eller om logging kun skal skje til et system med streng
        // tilgangskontroll (f.eks. sikker logg/audit-logg), før dette går til produksjon.
        logger.warn(
            "Fant ikke tvangsgrunnlag for skyldner={} oppdragsgiversKravidentifikator={} " +
                "skatteetatensKravidentifikator={}: ingen tilbakekrevingsdokumenter funnet i tilbakeløsningen " +
                "(registrertISkeKrav={})",
            request.skyldner,
            request.oppdragsgiversKravidentifikator,
            request.skatteetatensKravidentifikator,
            registrertISkeKrav,
        )
    }
}
