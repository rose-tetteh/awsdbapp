package com.example.awsdbapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {
    private static final Region REGION = Region.US_WEST_1;

    @Bean
    public static S3Client s3Client() {
        return S3Client.builder()
                .region(REGION)
                .credentialsProvider(AnonymousCredentialsProvider.create())
                .build();
    }

    @Bean
    public static S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(REGION)
                .credentialsProvider(AnonymousCredentialsProvider.create())
                .build();
    }
}