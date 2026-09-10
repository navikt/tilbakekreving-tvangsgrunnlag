package no.nav.tilbakekreving.tvangsgrunnlag.klient

// Klient mot sokos-ske-krav, brukt som alternativ kilde for å verifisere
// skatteetatensKravidentifikator dersom kravet ikke (ennå) finnes i tilbakeløsningen.
//
// TODO: Dette er foreløpig kun et interface med en midlertidig implementasjon uten ekte
// integrasjon. For reell integrasjon må:
//  - Avklare API-kontrakt mot sokos-ske-krav for oppslag på skatteetatensKravidentifikator
//  - Legge inn accessPolicy (outbound) mot sokos-ske-krav i .deploy/nais/*.yaml
//  - Velge autentiseringsmekanisme (f.eks. TokenX) for kall mellom tjenestene
//  - Erstatte den midlertidige implementasjonen med en ekte HTTP-klient
interface SkeKravClient {
    fun finnesKravidentifikator(skatteetatensKravidentifikator: String): Boolean
}

class MidlertidigSkeKravClient : SkeKravClient {
    override fun finnesKravidentifikator(skatteetatensKravidentifikator: String): Boolean = false
}
