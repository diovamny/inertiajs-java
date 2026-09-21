#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
# ========================================
# Native build: Spring Boot + Inertia.js (${artifactId})
# Builder keeps jdk-25 by explicit decision (see README)
# ========================================
FROM quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:jdk-25 AS builder
LABEL project="${artifactId}" \
      stage="builder"
USER root
WORKDIR /build
# 1. Cache the Maven Wrapper and the POM
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
# 2. Warm the dependency cache (online: the frontend build needs the network)
RUN ./mvnw dependency:go-offline -B -Pnative
# 3. Copy sources (Java + webui)
COPY src ./src
# 4. Native package: Spring AOT + GraalVM, frontend (npm install + build) included
RUN ./mvnw package \
    -B \
    --no-transfer-progress \
    -DskipTests \
    -Pnative
# 5. Prepare permissions for UBI Micro
RUN mkdir -p /app-root && \
    cp /build/target/${artifactId} /app-root/${artifactId} && \
    chown -R 1001:root /app-root && \
    chmod -R g+rwX /app-root
# ========================================
# Final UBI Micro image
# ========================================
FROM registry.access.redhat.com/ubi9/ubi-micro:latest
LABEL project="${artifactId}" \
      stage="runtime"
WORKDIR /app
# Inject libz.so.1 required by the native binary
COPY --from=builder \
     /usr/lib64/libz.so.1 \
     /usr/lib64/libz.so.1
# Copy the executable with the prepared permissions
COPY --from=builder \
     --chown=1001:root \
     /app-root \
     /app
EXPOSE 8080
USER 1001
ENTRYPOINT ["./${artifactId}"]
