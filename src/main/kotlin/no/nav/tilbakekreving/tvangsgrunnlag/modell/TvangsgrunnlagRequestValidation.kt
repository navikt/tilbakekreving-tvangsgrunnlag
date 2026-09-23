package no.nav.tilbakekreving.tvangsgrunnlag.modell

import io.konform.validation.Validation

/**
 * Validerer TvangsgrunnlagRequest deklarativt med Konform, koblet inn i Ktor via
 * `RequestValidation`-pluginen i ConfigureRouting.kt. Feil her gir 400 Ugyldig forespørsel.
 *
 * skatteetatensKravidentifikator valideres kun som ikke-blank streng. Referansespec-en
 * (https://navikt.github.io/tilbakekreving-api/) oppgir `format: uuid`, men dette er ikke
 * bekreftet mot faktisk format i kildesystemene - legg til UUID-validering igjen dersom/når det
 * er avklart at feltet faktisk alltid er en UUID.
 */
val tvangsgrunnlagRequestValidation =
    Validation<TvangsgrunnlagRequest> {
        TvangsgrunnlagRequest::skyldner {
            constrain("må ikke være blank") { it.isNotBlank() }
        }
        TvangsgrunnlagRequest::oppdragsgiversKravidentifikator {
            constrain("må ikke være blank") { it.isNotBlank() }
        }
        TvangsgrunnlagRequest::skatteetatensKravidentifikator {
            constrain("må ikke være blank") { it.isNotBlank() }
        }
    }
