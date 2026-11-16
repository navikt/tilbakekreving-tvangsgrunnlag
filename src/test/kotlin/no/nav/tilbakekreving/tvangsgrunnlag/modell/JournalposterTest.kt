package no.nav.tilbakekreving.tvangsgrunnlag.modell

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JournalposterTest {
    @Test
    fun `utledVedtakId fjerner OB04-prefiks og ledende nuller`() {
        assertEquals("123456", utledVedtakId("OB04000123456"))
        assertEquals("1020304", utledVedtakId("OB040001020304"))
        assertEquals("0", utledVedtakId("OB040000"))
    }

    @Test
    fun `utledVedtakId avviser ugyldig format`() {
        listOf("NAV-KRAV-2024-001", "OB04", "OB04ABC123", "OB04١٢٣", "OB049223372036854775808").forEach {
            assertNull(utledVedtakId(it))
        }
    }
}
