package com.example.awsdbapp.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    private final Region REGION = Region.US_WEST_1;

    @Value("${aws.secretsmanager.secretArn}")
    private String secretArn;

    @Value("${aws.accessKeyId:#{null}}")
    private String accessKeyId;

    @Value("${aws.secretKey:#{null}}")
    private String secretKey;

    @Bean
    @Primary
    public DataSource dataSource() {
        try {
            // Build the SecretsManager client
            SecretsManagerClient secretsClient;

            if (accessKeyId != null && secretKey != null) {
                // If explicit credentials are provided, use them
                logger.info("Using provided AWS credentials");
                secretsClient = SecretsManagerClient.builder()
                        .region(REGION)
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKeyId, secretKey)))
                        .build();
            } else {
                // Otherwise fall back to default credentials provider chain
                logger.info("Using default AWS credentials provider chain");
                secretsClient = SecretsManagerClient.builder()
                        .region(REGION)
                        .build();
            }

            logger.info("Fetching secret with ARN: {}", secretArn);
            GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                    .secretId(secretArn)
                    .build();

            GetSecretValueResponse response = secretsClient.getSecretValue(getSecretValueRequest);
            String secretString = response.secretString();
            logger.info("Secret retrieved successfully");

            // Parse the secret JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode secretJson = objectMapper.readTree(secretString);

            String engine = secretJson.has("engine") ? secretJson.get("engine").asText() : "postgresql";
            String host = secretJson.get("host").asText();
            String port = secretJson.get("port").asText();
            String dbname = secretJson.has("dbname") ? secretJson.get("dbname").asText() :
                    secretJson.has("dbInstanceIdentifier") ? secretJson.get("dbInstanceIdentifier").asText() : "postgres";
            String username = secretJson.get("username").asText();
            String password = secretJson.get("password").asText();

            String jdbcUrl = String.format("jdbc:%s://%s:%s/%s", engine, host, port, dbname);
            logger.info("JDBC URL constructed: {}", jdbcUrl);

            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .build();
        } catch (Exception e) {
            logger.error("Failed to get database credentials from Secrets Manager", e);
            throw new RuntimeException("Failed to get database credentials from Secrets Manager", e);
        }
    }
}