package no.nav.tilbakekreving.tvangsgrunnlag.tjeneste

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.slf4j.LoggerFactory

/**
 * Statistikk over hvor mange ganger et vedtak er utlevert til Skatteetaten.
 *
 * Vi bruker Loki til logger i dag, så hovedmekanismen er en strukturert loggmelding per
 * utlevering (feltet `dokumentId`) som kan telles/aggregeres med Loki-spørringer, f.eks.
 * `count_over_time({app="tilbakekreving-tvangsgrunnlag"} | logfmt | dokumentId="dok-1"[24h])`.
 *
 * Telleren eksponeres i tillegg som en Prometheus-metrikk (`tvangsgrunnlag_utlevering_total`)
 * via MeterRegistry, i stedet for en in-memory ConcurrentHashMap - historikken overlever da
 * en app-restart fordi Prometheus (ikke appen selv) lagrer måleseriene over tid.
 *
 * NB: dokumentId brukes som label, som gir én tidsserie per dokument som er utlevert. Dersom
 * antall unike dokumenter blir stort bør dette revurderes for å unngå høy kardinalitet i
 * Prometheus.
 */
class UtleveringsStatistikk(
    private val registry: MeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT),
) {
    private val logger = LoggerFactory.getLogger("statistikk.utlevering")

    fun registrerUtlevering(dokumentId: String) {
        val teller =
            Counter
                .builder("tvangsgrunnlag_utlevering_total")
                .description("Antall ganger et vedtak er utlevert til Skatteetaten")
                .tag("dokumentId", dokumentId)
                .register(registry)
        teller.increment()
        logger.info("dokumentId={} hendelse=utlevert_til_ske antallUtleveringer={}", dokumentId, teller.count().toInt())
    }
}
