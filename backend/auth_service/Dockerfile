FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Install the local core library before building auth_service.
COPY core/pom.xml core/pom.xml
COPY core/src core/src
RUN mvn -B -f core/pom.xml -DskipTests install

COPY auth_service/pom.xml auth_service/pom.xml
COPY auth_service/src auth_service/src
RUN mvn -B -f auth_service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install --no-install-recommends --yes curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system spring \
    && useradd --system --gid spring --home-dir /app spring

COPY --from=build /workspace/auth_service/target/auth_service-0.0.1-SNAPSHOT.jar /app/auth-service.jar

RUN chown spring:spring /app/auth-service.jar
USER spring

EXPOSE 8081

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/auth-service.jar"]
