package no.nav.tilbakekreving.tvangsgrunnlag.modell

// Maks 18 sifre sørger for at vedtakId alltid passer i en Long.
private val KRAVIDENTIFIKATOR_FORMAT = "OB04\\d{1,18}".toRegex()

/**
 * Validerer TvangsgrunnlagRequest, koblet inn i Ktor via `RequestValidation`-pluginen i
 * ConfigureRouting.kt. Returnerer en liste med feilmeldinger (tom liste betyr gyldig).
 */
fun validerTvangsgrunnlagRequest(request: TvangsgrunnlagRequest): List<String> =
    buildList {
        if (request.skyldner.isBlank()) add("skyldner må ikke være blank")
        if (!KRAVIDENTIFIKATOR_FORMAT.matches(request.oppdragsgiversKravidentifikator)) {
            add("oppdragsgiversKravidentifikator må være OB04 etterfulgt av 1-18 sifre")
        }
        if (request.skatteetatensKravidentifikator.isBlank()) add("skatteetatensKravidentifikator må ikke være blank")
    }
