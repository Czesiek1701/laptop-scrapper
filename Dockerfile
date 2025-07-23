# --- 1. Etap build: kompilacja aplikacji do JAR-a
FROM maven:3.8.7-openjdk-17-slim AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# --- 2. Etap runtime: uruchomienie gotowego JAR-a
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "app.jar"]
