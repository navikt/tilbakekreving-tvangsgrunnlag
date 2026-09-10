package no.nav.tilbakekreving.tvangsgrunnlag.modell

class UgyldigForespørselException(
    message: String,
) : RuntimeException(message)

class TvangsgrunnlagIkkeFunnetException(
    message: String,
) : RuntimeException(message)
