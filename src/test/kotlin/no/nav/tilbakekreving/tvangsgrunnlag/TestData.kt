package no.nav.tilbakekreving.tvangsgrunnlag

import no.nav.tilbakekreving.tvangsgrunnlag.modell.TvangsgrunnlagRequest

fun tvangsgrunnlagRequest(
    skyldner: String = "12345678901",
    oppdragsgiversKravidentifikator: String = "OB04000123456",
    skatteetatensKravidentifikator: String = "550e8400-e29b-41d4-a716-446655440000",
) = TvangsgrunnlagRequest(
    skyldner = skyldner,
    oppdragsgiversKravidentifikator = oppdragsgiversKravidentifikator,
    skatteetatensKravidentifikator = skatteetatensKravidentifikator,
)
