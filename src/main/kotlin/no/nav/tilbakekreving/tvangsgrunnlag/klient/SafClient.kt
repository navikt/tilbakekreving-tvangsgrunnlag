package no.nav.tilbakekreving.tvangsgrunnlag.klient

import no.nav.tilbakekreving.tvangsgrunnlag.modell.DokumentReferanse
import java.io.ByteArrayOutputStream

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
    override fun hentPdf(dokument: DokumentReferanse): ByteArray =
        byggEksempelPdf(
            tittel = dokument.tittel,
            linjer =
                listOf(
                    "DokumentId: ${dokument.dokumentId}",
                    "Dette er et eksempeldokument brukt til å kontrollere at zip-utpakking og PDF-visning fungerer.",
                    "Testinnhold frem til ekte SAF-integrasjon er på plass, se TODO i SafClient.kt.",
                ),
        )

    /**
     * Bygger en minimal, men gyldig, éns-siders PDF med ren tekst.
     * Ikke bruk denne funksjonen til å generere ekte tilbakekrevingsvedtak.
     */
    private fun byggEksempelPdf(
        tittel: String,
        linjer: List<String>,
    ): ByteArray {
        fun escape(tekst: String) = tekst.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")

        val innhold =
            buildString {
                append("BT\n/F1 16 Tf\n72 720 Td\n18 TL\n")
                append("(${escape(tittel)}) Tj\n")
                append("/F1 12 Tf\n")
                linjer.forEach { linje -> append("T*\n(${escape(linje)}) Tj\n") }
                append("ET")
            }
        val innholdBytes = innhold.toByteArray(Charsets.ISO_8859_1)

        val objekter =
            listOf(
                "<< /Type /Catalog /Pages 2 0 R >>",
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 2 0 R /Resources << /Font << /F1 4 0 R >> >> " +
                    "/MediaBox [0 0 612 792] /Contents 5 0 R >>",
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
                "<< /Length ${innholdBytes.size} >>\nstream\n$innhold\nendstream",
            )

        val ut = ByteArrayOutputStream()

        fun skriv(tekst: String) = ut.write(tekst.toByteArray(Charsets.ISO_8859_1))

        skriv("%PDF-1.4\n")
        val offsets =
            objekter.mapIndexed { indeks, body ->
                val offset = ut.size()
                skriv("${indeks + 1} 0 obj\n$body\nendobj\n")
                offset
            }
        val xrefOffset = ut.size()
        skriv("xref\n0 ${objekter.size + 1}\n0000000000 65535 f \n")
        offsets.forEach { offset -> skriv("%010d 00000 n \n".format(offset)) }
        skriv("trailer\n<< /Size ${objekter.size + 1} /Root 1 0 R >>\nstartxref\n$xrefOffset\n%%EOF")
        return ut.toByteArray()
    }
}
