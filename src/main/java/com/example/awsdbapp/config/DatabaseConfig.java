package com.example.awsdbapp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DatabaseConfig {

    private final SecretsManagerService secretsManagerService;

    @Bean
    public DataSource dataSource() {
        String secretJson = secretsManagerService.getSecretValue("dev/database-credentials");

        // Parse the JSON to extract individual values
        Map<String, String> secretValues = secretsManagerService.parseSecretJson(secretJson);

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");

        // Construct the JDBC URL using the host, port, and dbname from the secret
        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s",
                secretValues.get("host"),
                secretValues.get("port"),
                secretValues.get("dbname")
        );

        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(secretValues.get("username"));
        dataSource.setPassword(secretValues.get("password"));

        return dataSource;
    }
}