FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY gateway/pom.xml gateway/pom.xml
COPY gateway/src gateway/src
RUN mvn -B -f gateway/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install --no-install-recommends --yes curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system spring \
    && useradd --system --gid spring --home-dir /app spring

COPY --from=build /workspace/gateway/target/gateway-0.0.1-SNAPSHOT.jar /app/gateway.jar

RUN chown spring:spring /app/gateway.jar
USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/gateway.jar"]
