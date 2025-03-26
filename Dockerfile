FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
ARG SECRETS_MANAGER_SECRET_ARN
ENV SECRETS_MANAGER_SECRET_ARN=${SECRETS_MANAGER_SECRET_ARN}
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]