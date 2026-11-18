package no.nav.tilbakekreving.tvangsgrunnlag.modell

import kotlin.test.Test
import kotlin.test.assertEquals

class TvangsgrunnlagRequestValidationTest {
    private val gyldigRequest =
        TvangsgrunnlagRequest(
            skyldner = "12345678901",
            oppdragsgiversKravidentifikator = "OB04000123456",
            skatteetatensKravidentifikator = "550e8400-e29b-41d4-a716-446655440000",
        )

    @Test
    fun `ugyldig oppdragsgiversKravidentifikator`() {
        listOf("NAV-KRAV-2024-001", "OB04", "OB04${"1".repeat(19)}", "OB0412a").forEach { kravidentifikator ->
            val request = gyldigRequest.copy(oppdragsgiversKravidentifikator = kravidentifikator)
            assertEquals(
                listOf("oppdragsgiversKravidentifikator må være OB04 etterfulgt av 1-18 sifre"),
                validerTvangsgrunnlagRequest(request),
                "kravidentifikator=$kravidentifikator",
            )
        }
    }
}
