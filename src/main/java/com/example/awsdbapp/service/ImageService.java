package com.example.awsdbapp.service;

import com.example.awsdbapp.config.S3Config;
import com.example.awsdbapp.model.ImageDto;
import com.example.awsdbapp.model.ImageEntity;
import com.example.awsdbapp.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
    private final S3Client s3Client = S3Config.s3Client();
    private final S3Presigner s3Presigner = S3Config.s3Presigner();
    private final String bucketName = "imagegallery-buckett";

    private ImageRepository imageRepository;


//    public ImageService(ImageRepository imageRepository) {
//        this.s3Client = S3Config.s3Client();
//        this.s3Presigner = S3Config.s3Presigner();
//        this.imageRepository = imageRepository;
//    }


    public List<ImageDto> listImages() {
        try {
            return imageRepository.findAll().stream()
                    .map(entity -> ImageDto.builder()
                            .key(entity.getS3Key())
                            .url(generatePresignedUrl(entity.getS3Key()))
                            .title(entity.getTitle())
                            .description(entity.getDescription())
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error listing images: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Transactional
    public void uploadImage(MultipartFile file, String title, String description) throws IOException {
        try {
            String key = (title != null && !title.trim().isEmpty() ?
                    title.replaceAll("[^a-zA-Z0-9_]", "-") + "-" : "") +
                    System.currentTimeMillis() + "-" + file.getOriginalFilename();

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(
                    file.getInputStream(), file.getSize()));

            // Save metadata to database
            ImageEntity imageEntity = ImageEntity.builder()
                    .s3Key(key)
                    .title(title != null && !title.trim().isEmpty() ? title : "Untitled Image")
                    .description(description)
                    .contentType(file.getContentType())
                    .uploadedAt(LocalDateTime.now())
                    .build();

            imageRepository.save(imageEntity);
        } catch (Exception e) {
            log.error("Error uploading image to S3: {}", e.getMessage(), e);
            throw new IOException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteImage(String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);

            // Delete metadata from database
            imageRepository.deleteByS3Key(key);
        } catch (Exception e) {
            log.error("Error deleting image from S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete image: " + e.getMessage(), e);
        }
    }

    private String generatePresignedUrl(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(1))  // URL expires in 1 hour
                    .getObjectRequest(getObjectRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.error("Error generating presigned URL: {}", e.getMessage(), e);
            return s3Client.utilities()
                    .getUrl(builder -> builder.bucket(bucketName).key(key))
                    .toString();
        }
    }
}