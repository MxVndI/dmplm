package com.diplom.utils;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

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

    /**
     * Ensures the target bucket exists on startup. Fresh MinIO containers come
     * up without any buckets, and S3 PutObject fails with NoSuchBucket — which
     * is exactly what was blocking product-image uploads.
     *
     * Best-effort: if the broker is unreachable at boot the service still starts
     * and the bucket will be created on first upload attempt instead.
     */
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

    /**
     * Uploads a file to S3/MinIO and returns its public URL.
     */
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
            // Deletion failures shouldn't block the surrounding operation (e.g. replacing
            // an old photo); log and move on.
            log.warn("Failed to delete s3://{}/{}: {}", bucket, key, e.getMessage());
        }
    }

    private String buildPublicUrl(String key) {
        if (publicUrlBase != null && !publicUrlBase.isBlank()) {
            return publicUrlBase.stripTrailing() + "/" + bucket + "/" + key;
        }
        // No public URL configured → proxy through ImageController.
        // Works for MinIO (not publicly addressable from the browser) and AWS S3 alike.
        return "/images/" + key;
    }
}
