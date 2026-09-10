package no.nav.tilbakekreving.tvangsgrunnlag.klient

import no.nav.tilbakekreving.tvangsgrunnlag.modell.DokumentReferanse

// Klient mot SAF (Sak og Arkiv Fasade), brukt til å hente selve PDF-innholdet for
// tilbakekrevingsdokumentene som tilbakeløsningen har pekt ut.
//
// TODO: Dette er foreløpig kun et interface med en midlertidig implementasjon uten ekte
// integrasjon. For reell integrasjon må:
//  - Bruke SAFs GraphQL-/hentdokument-API for å hente PDF for en gitt journalpost/dokumentId
//  - Legge inn accessPolicy (outbound) mot SAF i .deploy/nais/*.yaml
//  - Velge autentiseringsmekanisme (Azure AD/TokenX) for kall mot SAF
//  - Erstatte den midlertidige implementasjonen med en ekte HTTP-klient
interface SafClient {
    fun hentPdf(dokument: DokumentReferanse): ByteArray
}

class MidlertidigSafClient : SafClient {
    override fun hentPdf(dokument: DokumentReferanse): ByteArray = "%PDF-1.4 midlertidig-innhold for ${dokument.dokumentId}".toByteArray()
}
