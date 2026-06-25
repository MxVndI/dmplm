package com.diplom.domain.service;

import com.diplom.persistance.entity.TestTemplateEntity;
import com.diplom.persistance.repository.TestTemplateRepository;
import com.diplom.utils.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestTemplateService {

    private final TestTemplateRepository repository;
    private final StorageService storageService;
    private final S3Client s3Client;

    @Value("${app.s3.bucket}")
    private String bucket;

    public List<TestTemplateEntity> findAll() {
        return repository.findAll();
    }

    public List<TestTemplateEntity> findByTestId(String testId) {
        return repository.findByTestId(testId);
    }

    public List<TestTemplateEntity> findByTestIdAndVariant(String testId, String variant) {
        return repository.findByTestIdAndVariant(testId, variant);
    }

    public Optional<TestTemplateEntity> findTemplateForPath(String testId, String variant, String path) {
        List<TestTemplateEntity> candidates = repository.findByTestIdAndVariant(testId, variant);
        if (candidates.isEmpty()) return Optional.empty();
        String normPath = path.endsWith("/") && path.length() > 1 ? path.substring(0, path.length() - 1) : path;
        for (TestTemplateEntity t : candidates) {
            if (normPath.equals(t.getPagePattern())) return Optional.of(t);
        }
        for (TestTemplateEntity t : candidates) {
            String pat = t.getPagePattern();
            if (pat != null && pat.endsWith("/*")) {
                String prefix = pat.substring(0, pat.length() - 1);
                if (normPath.startsWith(prefix) && !normPath.substring(prefix.length()).contains("/")) {
                    return Optional.of(t);
                }
            }
        }
        for (TestTemplateEntity t : candidates) {
            String pat = t.getPagePattern();
            if (pat != null && pat.endsWith("/**")) {
                String prefix = pat.substring(0, pat.length() - 2);
                if (normPath.startsWith(prefix)) return Optional.of(t);
            }
        }
        return Optional.empty();
    }

    public TestTemplateEntity upload(String testId, String variant, String pagePattern,
                                     String pageName, String name, MultipartFile file) {
        repository.findByTestIdAndVariantAndPagePattern(testId, variant, pagePattern).ifPresent(old -> {
            storageService.deleteFile(old.getMinioKey());
            repository.delete(old);
        });

        String id = UUID.randomUUID().toString();
        String minioKey = "templates/" + id + ".html";

        try {
            storageService.uploadFile(minioKey, file.getInputStream(), "text/html");
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file: " + e.getMessage(), e);
        }

        TestTemplateEntity template = new TestTemplateEntity();
        template.setId(id);
        template.setName(name);
        template.setTestId(testId);
        template.setVariant(variant);
        template.setPagePattern(pagePattern);
        template.setPageName(pageName);
        template.setMinioKey(minioKey);
        template.setOriginalFileName(file.getOriginalFilename());
        template.setUploadedAt(LocalDateTime.now());

        return repository.save(template);
    }

    public TestTemplateEntity uploadPreset(String testId, String variant, String pagePattern,
                                           String pageName, String name, String html) {
        repository.findByTestIdAndVariantAndPagePattern(testId, variant, pagePattern).ifPresent(old -> {
            storageService.deleteFile(old.getMinioKey());
            repository.delete(old);
        });

        String id = UUID.randomUUID().toString();
        String minioKey = "templates/" + id + ".html";

        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        storageService.uploadBytes(minioKey, bytes, "text/html");

        TestTemplateEntity template = new TestTemplateEntity();
        template.setId(id);
        template.setName(name);
        template.setTestId(testId);
        template.setVariant(variant);
        template.setPagePattern(pagePattern);
        template.setPageName(pageName);
        template.setMinioKey(minioKey);
        template.setOriginalFileName(name + ".html");
        template.setUploadedAt(LocalDateTime.now());

        return repository.save(template);
    }

    public String getHtmlContent(TestTemplateEntity template) {
        try {
            GetObjectRequest req = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(template.getMinioKey())
                    .build();
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(req);
            return new String(response.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read template content from MinIO: {}", e.getMessage());
            throw new RuntimeException("Could not load template content", e);
        }
    }

    public void delete(String id) {
        repository.findById(id).ifPresent(t -> {
            storageService.deleteFile(t.getMinioKey());
            repository.deleteById(id);
        });
    }
}
