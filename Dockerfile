FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV SECRETS_MANAGER_SECRET_ARN=${SECRETS_MANAGER_SECRET_ARN}
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]