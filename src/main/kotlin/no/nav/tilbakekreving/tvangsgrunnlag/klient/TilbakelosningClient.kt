package no.nav.tilbakekreving.tvangsgrunnlag.klient

import no.nav.tilbakekreving.tvangsgrunnlag.modell.DokumentReferanse
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
    // Henter tilbakekrevingsdokumenter (vedtak/endringsvedtak) registrert på kravet.
    // Returnerer tom liste dersom kravet, eller dokumenter på det, ikke finnes - endepunktet
    // svarer da 404 uten at klienten trenger en egen eksistenssjekk.
    fun hentDokumenter(
        skyldner: String,
        oppdragsgiversKravidentifikator: String,
        skatteetatensKravidentifikator: String,
    ): List<DokumentReferanse>
}

class MidlertidigTilbakelosningClient : TilbakelosningClient {
    override fun hentDokumenter(
        skyldner: String,
        oppdragsgiversKravidentifikator: String,
        skatteetatensKravidentifikator: String,
    ): List<DokumentReferanse> {
        if (!KJENTE_KRAV.contains(Triple(skyldner, oppdragsgiversKravidentifikator, skatteetatensKravidentifikator))) {
            return emptyList()
        }
        return listOf(
            DokumentReferanse("111111111", "1", LocalDate.of(2024, 1, 10)),
            DokumentReferanse("222222222", "1", LocalDate.of(2024, 6, 15)),
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
