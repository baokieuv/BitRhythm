FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Install the local core library before building auth_service.
COPY core/pom.xml core/pom.xml
COPY core/src core/src
RUN mvn -B -f core/pom.xml -DskipTests install

COPY notification_service/pom.xml notification_service/pom.xml
COPY notification_service/src notification_service/src
RUN mvn -B -f notification_service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install --no-install-recommends --yes curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system spring \
    && useradd --system --gid spring --home-dir /app spring

COPY --from=build /workspace/notification_service/target/notification_service-0.0.1-SNAPSHOT.jar /app/notification-service.jar

RUN chown spring:spring /app/notification-service.jar
USER spring

EXPOSE 8084

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/notification-service.jar"]
