FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
#ARG SECRETS_MANAGER_SECRET_ARN
#ARG AWS_ACCESS_KEY_ID
#ARG AWS_SECRET_ACCESS_KEY
#
#ENV SECRETS_MANAGER_SECRET_ARN=${SECRETS_MANAGER_SECRET_ARN}
#ENV AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
#ENV AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]