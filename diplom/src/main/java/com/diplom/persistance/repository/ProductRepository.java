package com.diplom.persistance.repository;

import com.diplom.persistance.entity.ProductEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository

public interface ProductRepository extends MongoRepository<ProductEntity, String> {
    List<ProductEntity> findByNameContainingIgnoreCase(String name);
    List<ProductEntity> findByAvailableQuantityGreaterThan(int quantity);
}
