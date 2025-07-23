# --- 1. Etap build: kompilacja aplikacji do JAR-a
FROM maven:3.8.4-jdk-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# --- 2. Etap runtime: uruchomienie gotowego JAR-a
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

