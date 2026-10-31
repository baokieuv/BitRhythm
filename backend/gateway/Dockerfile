FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Dependencies first, so this layer is cached until pom.xml changes
COPY gateway/pom.xml gateway/pom.xml
RUN mvn -B -f gateway/pom.xml dependency:go-offline

COPY gateway/src gateway/src
RUN mvn -B -f gateway/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install --no-install-recommends --yes curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system spring \
    && useradd --system --gid spring --home-dir /app spring

COPY --from=build /workspace/gateway/target/gateway.jar /app/gateway.jar

RUN chown spring:spring /app/gateway.jar
USER spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD curl --fail http://localhost:8080/health/liveness || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/gateway.jar"]
