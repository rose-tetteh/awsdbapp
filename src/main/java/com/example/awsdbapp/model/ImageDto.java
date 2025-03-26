package com.example.awsdbapp.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageDto {
    private String key;
    private String url;
    private String title;
    private String description;

}