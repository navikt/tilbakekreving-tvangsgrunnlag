package no.nav.tilbakekreving.tvangsgrunnlag.klient

import no.nav.tilbakekreving.tvangsgrunnlag.modell.DokumentReferanse
import org.slf4j.LoggerFactory
import java.util.UUID

// Klient mot Joark (dokarkiv), brukt til å registrere at dokumenter er utlevert til Skatteetaten.
//
// TODO: Dette er foreløpig kun et interface med en midlertidig implementasjon uten ekte
// integrasjon. For reell integrasjon må:
//  - Avklare hvilket dokarkiv-API/operasjon som skal brukes for å registrere utlevering
//  - Legge inn accessPolicy (outbound) mot dokarkiv i .deploy/nais/*.yaml
//  - Velge autentiseringsmekanisme (Azure AD) for kall mot Joark
//  - Erstatte den midlertidige implementasjonen med en ekte HTTP-klient
interface JoarkClient {
    fun registrerUtlevering(
        skyldner: String,
        dokumenter: List<DokumentReferanse>,
    ): String
}

class MidlertidigJoarkClient : JoarkClient {
    private val logger = LoggerFactory.getLogger(MidlertidigJoarkClient::class.java)

    override fun registrerUtlevering(
        skyldner: String,
        dokumenter: List<DokumentReferanse>,
    ): String {
        val journalpostId = UUID.randomUUID().toString()
        logger.info(
            "Midlertidig registrering av utlevering av {} dokument(er) til SKE i Joark, journalpostId={}",
            dokumenter.size,
            journalpostId,
        )
        return journalpostId
    }
}
