package no.nav.tilbakekreving.tvangsgrunnlag.tjeneste

import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Utgangspunkt for statistikk over hvor mange ganger et vedtak er utlevert til Skatteetaten.
 *
 * Vi bruker Loki til logger i dag, så hovedmekanismen er en strukturert loggmelding per
 * utlevering (feltet `dokumentId`) som kan telles/aggregeres med Loki-spørringer, f.eks.
 * `count_over_time({app="tilbakekreving-tvangsgrunnlag"} | logfmt | dokumentId="dok-1"[24h])`.
 *
 * Telleren i minnet er kun et supplement for rask innsikt i samme instans, og nullstilles ved
 * restart. Bytt til en persistent løsning (f.eks. dedikert metrikk-backend) dersom historikk på
 * tvers av restarter/instanser blir nødvendig.
 */
class UtleveringsStatistikk {
    private val logger = LoggerFactory.getLogger("statistikk.utlevering")
    private val tellere = ConcurrentHashMap<String, AtomicInteger>()

    fun registrerUtlevering(dokumentId: String) {
        val antall = tellere.computeIfAbsent(dokumentId) { AtomicInteger(0) }.incrementAndGet()
        logger.info("dokumentId={} hendelse=utlevert_til_ske antallUtleveringer={}", dokumentId, antall)
    }
}
