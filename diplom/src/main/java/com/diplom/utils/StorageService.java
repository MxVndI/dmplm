package com.diplom.utils;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final S3Client s3Client;

    @Value("${app.s3.bucket}")
    private String bucket;

    @Value("${app.s3.public-url-base:}")
    private String publicUrlBase;

    @Value("${app.s3.endpoint:}")
    private String endpoint;

    @PostConstruct
    public void ensureBucketExists() {
        if (bucket == null || bucket.isBlank()) {
            log.warn("S3 bucket not configured (app.s3.bucket) — uploads will fail");
            return;
        }
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            log.info("S3 bucket '{}' is reachable", bucket);
        } catch (NoSuchBucketException e) {
            try {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
                log.info("S3 bucket '{}' created on startup", bucket);
            } catch (SdkException ce) {
                log.warn("Failed to create S3 bucket '{}': {}", bucket, ce.getMessage());
            }
        } catch (SdkException e) {
            log.warn("Could not verify S3 bucket '{}' at startup: {}", bucket, e.getMessage());
        }
    }

    public String uploadFile(String key, InputStream inputStream, String contentType) {
        try {
            byte[] bytes = inputStream.readAllBytes();
            return uploadBytes(key, bytes, contentType);
        } catch (IOException e) {
            log.error("Failed to read file stream for key={} ({})", key, e.getMessage());
            throw new RuntimeException("Failed to read file stream: " + e.getMessage(), e);
        }
    }

    public String uploadBytes(String key, byte[] bytes, String contentType) {
        String resolvedType = (contentType == null || contentType.isBlank())
                ? DEFAULT_CONTENT_TYPE : contentType;
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(resolvedType)
                    .contentLength((long) bytes.length)
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(bytes));
            String url = buildPublicUrl(key);
            log.info("Uploaded {} bytes to s3://{}/{} ({})", bytes.length, bucket, key, resolvedType);
            return url;
        } catch (SdkException e) {
            log.error("S3 upload failed bucket={} key={} contentType={}: {}",
                    bucket, key, resolvedType, e.getMessage(), e);
            throw new RuntimeException("Не удалось загрузить файл в S3: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            log.debug("Deleted s3://{}/{}", bucket, key);
        } catch (SdkException e) {
            log.warn("Failed to delete s3://{}/{}: {}", bucket, key, e.getMessage());
        }
    }

    private String buildPublicUrl(String key) {
        if (publicUrlBase != null && !publicUrlBase.isBlank()) {
            return publicUrlBase.stripTrailing() + "/" + bucket + "/" + key;
        }
        return "/images/" + key;
    }
}
