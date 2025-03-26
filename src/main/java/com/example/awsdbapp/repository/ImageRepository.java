package com.example.awsdbapp.repository;

import com.example.awsdbapp.model.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Long> {
    Optional<ImageEntity> findByS3Key(String s3Key);
    void deleteByS3Key(String s3Key);
}