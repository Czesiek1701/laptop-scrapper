# 1. Etap build: kompilacja aplikacji do JAR-a
FROM maven:3.8.4-jdk-17 AS build
WORKDIR /app

# Kopiujemy definicję zależności i kod źródłowy
COPY pom.xml .
COPY src ./src

# Budujemy aplikację, pomijając testy
RUN mvn clean package -DskipTests

# 2. Etap runtime: uruchomienie gotowego JAR-a
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Kopiujemy JAR z poprzedniego etapu
COPY --from=build /app/target/*.jar app.jar

# Deklarujemy port, na którym nasłuchuje Spring Boot
EXPOSE 10000

# Uruchamiamy aplikację
ENTRYPOINT ["java", "-jar", "app.jar"]
