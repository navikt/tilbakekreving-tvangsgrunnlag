package no.nav.tilbakekreving.tvangsgrunnlag.modell

import no.nav.tilbakekreving.tvangsgrunnlag.tvangsgrunnlagRequest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals

class TvangsgrunnlagRequestValidationTest {
    @ParameterizedTest
    @ValueSource(strings = ["NAV-KRAV-2024-001", "OB04", "OB041111111111111111111", "OB0412a"])
    fun `ugyldig oppdragsgiversKravidentifikator`(kravidentifikator: String) {
        val request = tvangsgrunnlagRequest(oppdragsgiversKravidentifikator = kravidentifikator)
        assertEquals(
            listOf("oppdragsgiversKravidentifikator må være OB04 etterfulgt av 1-18 sifre"),
            validerTvangsgrunnlagRequest(request),
        )
    }
}
