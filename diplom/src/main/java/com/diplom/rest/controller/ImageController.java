package com.diplom.rest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;

/**
 * Streams product images from S3/MinIO through the shop itself.
 *
 * Why proxy instead of letting the browser fetch directly from MinIO?
 *   - MinIO inside docker is reachable at "minio:9000" but that hostname isn't
 *     resolvable from the user's browser.
 *   - Mapping a host port (e.g. 9000:9000) works but forces users to configure
 *     S3_PUBLIC_URL correctly and exposes MinIO publicly.
 *   - Proxying through the shop keeps the only public hostname = shop's, and
 *     stays correct in both dev (MinIO) and prod (AWS S3) with no config drift.
 */
@Slf4j
@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

    private final S3Client s3Client;

    @Value("${app.s3.bucket}")
    private String bucket;

    /**
     * {@code /{*key}} captures the FULL remaining path including slashes
     * (PathPatternParser syntax — Spring Boot 3.x default).
     * Required because photo keys are of the form "products/{uuid}/{filename}"
     * and a plain {key:.+} pattern only matches a single path segment.
     */
    @GetMapping("/{*key}")
    public ResponseEntity<byte[]> getImage(@PathVariable String key) {
        // {*key} captures the path starting with "/" — strip it before passing to S3.
        if (key.startsWith("/")) key = key.substring(1);
        if (key.isBlank()) return ResponseEntity.notFound().build();

        try (ResponseInputStream<GetObjectResponse> stream = s3Client.getObject(
                GetObjectRequest.builder().bucket(bucket).key(key).build())) {

            byte[] body = stream.readAllBytes();
            String contentType = stream.response().contentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                    .body(body);

        } catch (NoSuchKeyException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.warn("Failed to stream image '{}': {}", key, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
