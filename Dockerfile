# ── Stage 1: Build the application with Gradle ──────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Gradle wrapper and build files for better caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src src

# Build the application with layered JAR enabled
RUN ./gradlew bootJar --no-daemon -x test

# Extract the JAR for layered optimization
RUN java -Djarmode=layertools -jar build/libs/webapp-k8s-demo-1.0-SNAPSHOT.jar extract

# ── Stage 2: Minimal runtime image ──────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Security: create a dedicated non-root user and group (explicit UID/GID 100/101)
RUN addgroup -S -g 101 appgroup && adduser -S -u 100 -G appgroup appuser

WORKDIR /app

# Copy layered application from builder (better caching: deps change rarely)
COPY --from=builder --chown=appuser:appgroup /build/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /build/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /build/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /build/application/ ./

# Switch to non-root user
USER appuser

# JVM tuning for containers (respect cgroup memory limits)
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.backgroundpreinitializer.ignore=true"

EXPOSE 8080

# Health check baked into the image
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/hello || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]