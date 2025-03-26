package com.example.awsdbapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.Optional;

@Service
public class SecretsManagerService {
    private static final Logger logger = LoggerFactory.getLogger(SecretsManagerService.class);

    private final SecretsManagerClient secretsManagerClient;

    public SecretsManagerService() {
        this.secretsManagerClient = SecretsManagerClient.builder()
                .region(Region.EU_WEST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public Optional<String> getSecretValue(String secretName) {
        try {
            logger.info("Attempting to retrieve secret: {}", secretName);

            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);

            logger.info("Successfully retrieved secret");
            return Optional.ofNullable(response.secretString());
        } catch (Exception e) {
            logger.error("Error retrieving secret: {}", secretName, e);
            return Optional.empty();
        }
    }
}