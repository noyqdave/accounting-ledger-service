# Use a Maven image with JDK 17 for build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY . .
RUN mvn clean verify

# Optionally: use JAR-only image if deploying
# FROM eclipse-temurin:17-jdk-alpine AS runtime
# WORKDIR /app
# COPY --from=build /app/target/ledger-service*.jar app.jar
# ENTRYPOINT ["java", "-jar", "app.jar"]