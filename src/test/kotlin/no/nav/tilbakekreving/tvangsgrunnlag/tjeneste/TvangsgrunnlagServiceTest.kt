package no.nav.tilbakekreving.tvangsgrunnlag.tjeneste

import kotlinx.coroutines.runBlocking
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigJoarkClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigSafClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigSkeKravClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.MidlertidigTilbakelosningClient
import no.nav.tilbakekreving.tvangsgrunnlag.klient.TilbakekrevingBackendClient
import no.nav.tilbakekreving.tvangsgrunnlag.modell.TvangsgrunnlagRequest
import no.nav.tilbakekreving.tvangsgrunnlag.modell.VedtaksdokumentReferanse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class TvangsgrunnlagServiceTest {
    private val request =
        TvangsgrunnlagRequest(
            skyldner = "12345678901",
            oppdragsgiversKravidentifikator = "OB04000123456",
            skatteetatensKravidentifikator = "550e8400-e29b-41d4-a716-446655440000",
        )

    private fun service(backend: TilbakekrevingBackendClient) =
        TvangsgrunnlagServiceImpl(
            backend,
            MidlertidigTilbakelosningClient(),
            MidlertidigSkeKravClient(),
            MidlertidigSafClient(),
            MidlertidigJoarkClient(),
        )

    @Test
    fun `vedtakId utledes uten prefiks og ledende nuller`() =
        runBlocking<Unit> {
            var mottattVedtakId: String? = null
            val backend =
                object : TilbakekrevingBackendClient {
                    override suspend fun hentVedtaksdokumenter(vedtakId: String): List<VedtaksdokumentReferanse> {
                        mottattVedtakId = vedtakId
                        return listOf(VedtaksdokumentReferanse("111", "1"))
                    }
                }

            service(backend).hentTvangsgrunnlag(request)
            assertEquals("123456", mottattVedtakId)
        }

    @Test
    fun `tom liste fra tilbakekreving-backend`() =
        runBlocking<Unit> {
            val backend =
                object : TilbakekrevingBackendClient {
                    override suspend fun hentVedtaksdokumenter(vedtakId: String) = emptyList<VedtaksdokumentReferanse>()
                }

            assertNull(service(backend).hentTvangsgrunnlag(request))
        }

    @Test
    fun `feil fra tilbakekreving-backend`() =
        runBlocking<Unit> {
            val backend =
                object : TilbakekrevingBackendClient {
                    override suspend fun hentVedtaksdokumenter(vedtakId: String): List<VedtaksdokumentReferanse> = error("nede")
                }

            assertFailsWith<TilbakekrevingBackendException> { service(backend).hentTvangsgrunnlag(request) }
        }
}
