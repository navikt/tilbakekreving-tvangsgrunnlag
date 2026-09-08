package no.nav.tilbakekreving.tvangsgrunnlag

import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import no.nav.security.token.support.v3.tokenValidationSupport

/**
 * Navn på issuer-konfigurasjonen, brukt både her og i application.conf
 * samt i `authenticate(MASKINPORTEN)` i ConfigureRouting.kt.
 */
const val MASKINPORTEN = "maskinporten"

/**
 * Scopet Skatteetaten må presentere for å få tilgang, jf. `consumers`/`scopes`
 * i .deploy/nais/app-dev.yaml (produkt "arbeid" + navn "scope.read" + separator "/").
 *
 * 🔴 Rød sone: verifiser denne strengen mot faktisk utstedt token fra Maskinporten
 * (Nais Console / testkall fra Skatteetaten) etter første deploy - formatet kan
 * avvike fra antagelsen under.
 */
const val PAKREVD_SCOPE = "arbeid/scope.read"

/**
 * 🔴 Rød sone — forstå denne grundig før merge.
 *
 * Validerer at innkommende token:
 * - er signert av Maskinporten (issuer + JWKS hentes automatisk fra
 *   MASKINPORTEN_WELL_KNOWN_URL, injisert av Nais når maskinporten.enabled=true)
 * - har riktig audience (MASKINPORTEN_CLIENT_ID)
 * - ikke er utløpt
 * - har scope-claimet PAKREVD_SCOPE (håndhevet i additionalValidation under -
 *   uten dette ville ethvert gyldig Maskinporten-token, uansett scope, sluppet gjennom)
 *
 * `tokenValidationSupport` leser resten av konfigurasjonen (issuer-alias
 * "maskinporten") fra application.conf, som peker på Nais-injiserte env-vars.
 */
fun Application.configureAuthentication() {
    val applicationConfig = environment.config
    authentication {
        tokenValidationSupport(
            name = MASKINPORTEN,
            config = applicationConfig,
            additionalValidation = { tokenValidationContext ->
                val scope = tokenValidationContext.getClaims(MASKINPORTEN)?.getStringClaim("scope")
                scope != null && scope.split(" ").contains(PAKREVD_SCOPE)
            },
        )
    }
}
