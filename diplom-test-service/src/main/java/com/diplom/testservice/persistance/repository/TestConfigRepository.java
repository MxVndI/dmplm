package com.diplom.testservice.persistance.repository;

import com.diplom.testservice.persistance.entity.TestConfigEntity;
import com.diplom.testservice.persistance.entity.TestStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TestConfigRepository extends MongoRepository<TestConfigEntity, String> {
    List<TestConfigEntity> findByStatus(TestStatus status);
}
