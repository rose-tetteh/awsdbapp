package com.example.awsdbapp.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Value("${aws.region}")
    private String awsRegion;

    @Value("${aws.secretsmanager.secretArn}")
    private String secretArn;

    @Bean
    @Primary
    public DataSource dataSource() {
        try {
            SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                    .region(Region.of(awsRegion))
                    .build();

            GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                    .secretId(secretArn)
                    .build();

            GetSecretValueResponse response = secretsClient.getSecretValue(getSecretValueRequest);
            String secretString = response.secretString();

            // Parse the secret JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode secretJson = objectMapper.readTree(secretString);

            String host = secretJson.get("host").asText();
            String port = secretJson.get("port").asText();
            String dbname = secretJson.get("dbname").asText();
            String username = secretJson.get("username").asText();
            String password = secretJson.get("password").asText();

            String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", host, port, dbname);

            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get database credentials from Secrets Manager", e);
        }
    }
}