package no.nav.tilbakekreving.tvangsgrunnlag.modell

/**
 * Validerer TvangsgrunnlagRequest, koblet inn i Ktor via `RequestValidation`-pluginen i
 * ConfigureRouting.kt. Returnerer en liste med feilmeldinger (tom liste betyr gyldig).
 */
fun validerTvangsgrunnlagRequest(request: TvangsgrunnlagRequest): List<String> =
    buildList {
        if (request.skyldner.isBlank()) add("skyldner må ikke være blank")
        if (request.oppdragsgiversKravidentifikator.isBlank()) add("oppdragsgiversKravidentifikator må ikke være blank")
        if (request.skatteetatensKravidentifikator.isBlank()) add("skatteetatensKravidentifikator må ikke være blank")
    }
