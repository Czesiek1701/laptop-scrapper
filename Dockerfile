# Etap budowania
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN chmod +x ./mvnw
RUN MAVEN_CONFIG="" ./mvnw clean package -DskipTests

COPY webdrivers/geckodriver /usr/local/bin/geckodriver
RUN chmod +x /usr/local/bin/geckodriver


# Etap uruchomienia
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render dynamicznie przypisuje port, więc musimy go uwzględnić
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
