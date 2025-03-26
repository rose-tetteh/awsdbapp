FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]