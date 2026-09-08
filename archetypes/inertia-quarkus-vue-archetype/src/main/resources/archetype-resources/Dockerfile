#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
# ========================================
# Native build: Quarkus + Inertia.js (${artifactId})
# Builder keeps jdk-25 by explicit decision (see README).
# This image already carries zlib, which GraalVM needs for linking.
# ========================================
FROM quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:jdk-25 AS build
LABEL project="${artifactId}" \
      stage="builder"
WORKDIR /code
COPY --chown=quarkus:quarkus mvnw /code/mvnw
COPY --chown=quarkus:quarkus .mvn /code/.mvn
COPY --chown=quarkus:quarkus pom.xml /code/
USER quarkus
RUN ./mvnw -B org.apache.maven.plugins:maven-dependency-plugin:3.8.0:go-offline -Pnative
COPY --chown=quarkus:quarkus src /code/src
RUN ./mvnw package -B --no-transfer-progress -DskipTests -Pnative

FROM registry.access.redhat.com/ubi9/ubi-micro:latest
LABEL project="${artifactId}" \
      stage="runtime"
WORKDIR /work/
# Copy zlib from the build stage
COPY --from=build /usr/lib64/libz.so.1 /usr/lib64/libz.so.1
RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work
COPY --chown=1001:root --chmod=0755 --from=build /code/target/*-runner /work/application
EXPOSE 8080
USER 1001
ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0"]
