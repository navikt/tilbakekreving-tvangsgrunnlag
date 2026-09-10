package no.nav.tilbakekreving.tvangsgrunnlag.klient

import no.nav.tilbakekreving.tvangsgrunnlag.modell.DokumentReferanse
import no.nav.tilbakekreving.tvangsgrunnlag.modell.DokumentType
import java.time.LocalDate

// Klient mot tilbakeløsningen (tilbakekrevingsapplikasjonen), som er kilden til krav- og
// dokumentinformasjon.
//
// TODO: Dette er foreløpig kun et interface med en midlertidig implementasjon uten ekte
// integrasjon. For reell integrasjon må:
//  - Avklare hvilket API/endepunkt i tilbakeløsningen som eksponerer krav- og dokumentoppslag
//  - Legge inn accessPolicy (outbound) mot riktig applikasjon i .deploy/nais/*.yaml
//  - Velge autentiseringsmekanisme (f.eks. TokenX) for kall mellom tjenestene
//  - Erstatte den midlertidige implementasjonen med en ekte HTTP-klient (Ktor HttpClient) med
//    timeout-/retry-håndtering og feilmapping
interface TilbakelosningClient {
    // Sjekker om skyldner, oppdragsgiversKravidentifikator og skatteetatensKravidentifikator er registrert på samme krav.
    fun finnesKrav(
        skyldner: String,
        oppdragsgiversKravidentifikator: String,
        skatteetatensKravidentifikator: String,
    ): Boolean

    // Henter tilbakekrevingsdokumenter (vedtak/endringsvedtak) registrert på kravet.
    fun hentDokumenter(
        skyldner: String,
        oppdragsgiversKravidentifikator: String,
        skatteetatensKravidentifikator: String,
    ): List<DokumentReferanse>
}

class MidlertidigTilbakelosningClient : TilbakelosningClient {
    override fun finnesKrav(
        skyldner: String,
        oppdragsgiversKravidentifikator: String,
        skatteetatensKravidentifikator: String,
    ): Boolean = KJENTE_KRAV.contains(Triple(skyldner, oppdragsgiversKravidentifikator, skatteetatensKravidentifikator))

    override fun hentDokumenter(
        skyldner: String,
        oppdragsgiversKravidentifikator: String,
        skatteetatensKravidentifikator: String,
    ): List<DokumentReferanse> {
        if (!finnesKrav(skyldner, oppdragsgiversKravidentifikator, skatteetatensKravidentifikator)) {
            return emptyList()
        }
        return listOf(
            DokumentReferanse("dok-1", DokumentType.VEDTAK, "Vedtak om tilbakekreving", LocalDate.of(2024, 1, 10)),
            DokumentReferanse("dok-2", DokumentType.ENDRINGSVEDTAK, "Endringsvedtak", LocalDate.of(2024, 6, 15)),
        )
    }

    companion object {
        // Testdata frem til ekte integrasjon er på plass.
        private val KJENTE_KRAV =
            setOf(
                Triple("12345678901", "NAV-KRAV-2024-001", "550e8400-e29b-41d4-a716-446655440000"),
            )
    }
}
