# syntax=docker/dockerfile:1

# ---- Build stage: compile and package the Spring Boot jar ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Resolve dependencies first so they are cached between source-only changes.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests \
    && cp target/jobtrack-backend-*.jar app.jar

# ---- Runtime stage: slim JRE image running as a non-root user ----
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd --system --create-home --uid 10001 jobtrack \
    && mkdir -p /app/uploads \
    && chown -R jobtrack:jobtrack /app
USER jobtrack

COPY --from=build --chown=jobtrack:jobtrack /app/app.jar app.jar

# Uploaded documents are written here; mount a volume to keep them across restarts.
ENV APPLICATION_STORAGE_UPLOAD_DIR=/app/uploads
VOLUME ["/app/uploads"]

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
