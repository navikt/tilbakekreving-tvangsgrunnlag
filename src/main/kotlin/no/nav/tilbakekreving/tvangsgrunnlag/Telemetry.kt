package no.nav.tilbakekreving.tvangsgrunnlag

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk

/**
 * Konfigurerer OpenTelemetry SDK-en automatisk basert på standard `OTEL_*`-miljøvariabler
 * (f.eks. `OTEL_EXPORTER_OTLP_ENDPOINT`, `OTEL_SERVICE_NAME`), slik disse typisk settes opp av
 * Nais-plattformen/Grafana Alloy-sidecaren.
 *
 * Uten disse miljøvariablene faller SDK-en tilbake til en no-op-eksportør, så tjenesten
 * oppfører seg som normalt (bare uten faktisk sporing) når det kjøres lokalt uten en
 * OTel-collector tilgjengelig.
 *
 * `OTEL_TRACES_EXPORTER`/`OTEL_METRICS_EXPORTER`/`OTEL_LOGS_EXPORTER` settes til "none" som
 * standardverdi, siden vi ikke har lagt til noen OTLP-eksportør på classpath. Uten dette prøver
 * autokonfigureringen å bruke OTLP som standard og feiler med en ConfigurationException.
 * Miljøvariabler satt eksternt (f.eks. av Nais) overstyrer fortsatt disse standardverdiene.
 */
fun konfigurerOpenTelemetry(): OpenTelemetry =
    AutoConfiguredOpenTelemetrySdk
        .builder()
        .addPropertiesSupplier {
            mapOf(
                "otel.traces.exporter" to "none",
                "otel.metrics.exporter" to "none",
                "otel.logs.exporter" to "none",
            )
        }.build()
        .openTelemetrySdk
