package com.diplom.persistance.repository;

import com.diplom.persistance.entity.ABTestEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ABTestRepository extends MongoRepository<ABTestEntity, String> {
    List<ABTestEntity> findByActive(boolean active);
}
