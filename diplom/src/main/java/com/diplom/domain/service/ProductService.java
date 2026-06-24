package com.diplom.domain.service;

import com.diplom.constant.AppConstants;
import com.diplom.mapper.ProductMapper;
import com.diplom.rest.dto.ProductDto;
import com.diplom.persistance.entity.ProductEntity;
import com.diplom.persistance.repository.ProductRepository;
import com.diplom.utils.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final StorageService storageService;
    private final ProductMapper productMapper;

    public List<ProductEntity> findAll() {
        return productRepository.findAll();
    }

    public Optional<ProductEntity> findById(String id) {
        return productRepository.findById(id);
    }

    public List<ProductEntity> search(String query) {
        return productRepository.findByNameContainingIgnoreCase(query);
    }

    public ProductEntity create(ProductDto dto, MultipartFile photo) throws IOException {
        ProductEntity product = productMapper.toEntity(dto);
        product.setCreatedAt(LocalDateTime.now());

        if (photo != null && !photo.isEmpty()) {
            String key = buildPhotoKey(photo.getOriginalFilename());
            String url = storageService.uploadFile(key, photo.getInputStream(), photo.getContentType());
            product.setPhotoKey(key);
            product.setPhotoUrl(url);
        }

        return productRepository.save(product);
    }

    public ProductEntity update(String id, ProductDto dto, MultipartFile photo) throws IOException {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.PRODUCT_NOT_FOUND + id));

        productMapper.updateEntityFromDto(dto, product);

        if (photo != null && !photo.isEmpty()) {
            if (product.getPhotoKey() != null) {
                storageService.deleteFile(product.getPhotoKey());
            }
            String key = buildPhotoKey(photo.getOriginalFilename());
            String url = storageService.uploadFile(key, photo.getInputStream(), photo.getContentType());
            product.setPhotoKey(key);
            product.setPhotoUrl(url);
        }

        return productRepository.save(product);
    }

    public void delete(String id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.PRODUCT_NOT_FOUND + id));
        if (product.getPhotoKey() != null) {
            storageService.deleteFile(product.getPhotoKey());
        }
        productRepository.delete(product);
    }

    private String buildPhotoKey(String originalFilename) {
        String sanitized = (originalFilename == null ? AppConstants.FALLBACK_FILE_NAME : originalFilename)
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        return AppConstants.PRODUCTS_FOLDER + UUID.randomUUID() + "/" + sanitized;
    }
}
