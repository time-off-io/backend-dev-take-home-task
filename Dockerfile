# --- Build stage ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Cache dependencies first
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline

# Build the application (skip tests here; the CI pipeline runs them)
COPY src/ src/
RUN ./mvnw -B -q clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# curl is used by the container health check
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Run as a non-root user
RUN groupadd --system app && useradd --system --gid app app
USER app

COPY --from=build /workspace/target/timeoff-assignment.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
