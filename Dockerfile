# --- 1. Etap build: kompilacja aplikacji do JAR-a
FROM maven:3.8.6-openjdk-17-slim AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# --- 2. Etap runtime: uruchomienie gotowego JAR-a
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Kopiujemy zbudowany plik JAR
COPY --from=build /app/target/*.jar app.jar

# Dokumentacyjnie deklarujemy port, pod którym nasłuchuje aplikacja
EXPOSE 10000

# Uruchamiamy aplikację
ENTRYPOINT ["java", "-jar", "app.jar"]
