FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25
COPY --chown=nonroot:nonroot ./build/libs/tilbakekreving-tvangsgrunnlag-all.jar /app/app.jar
WORKDIR /app

ENV APP_NAME=tilbakekreving-tvangsgrunnlag
ENV TZ="Europe/Oslo"
ENTRYPOINT [ "java", "-XX:MinRAMPercentage=25.0", "-XX:MaxRAMPercentage=75.0", "-XX:+HeapDumpOnOutOfMemoryError", "-jar", "/app/app.jar" ]