package com.example.awsdbapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseConfig {

    @Autowired
    private final SecretsManagerService secretsManagerService;
    private final ObjectMapper objectMapper;

    @Bean
    public DataSource dataSource() {
        try {
            // Retrieve secret from AWS Secrets Manager
            String secretJson = secretsManagerService.getSecretValue("dev/database-credentials")
                    .orElseThrow(() -> new RuntimeException("Failed to retrieve secret"));

            // Parse the secret JSON
            Map<String, String> secretValues = objectMapper.readValue(secretJson, Map.class);

            // Construct JDBC URL
            String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s",
                    secretValues.get("host"),
                    secretValues.get("port"),
                    secretValues.get("dbname")
            );

            // Build DataSource using the parsed credentials
            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(secretValues.get("username"))
                    .password(secretValues.get("password"))
                    .driverClassName("org.postgresql.Driver")
                    .build();

        } catch (Exception e) {
            log.error("Error creating DataSource", e);
            throw new RuntimeException("Could not create DataSource", e);
        }
    }
}