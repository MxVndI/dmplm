package com.diplom.persistance.repository;

import com.diplom.persistance.entity.TestTemplateEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestTemplateRepository extends MongoRepository<TestTemplateEntity, String> {
    List<TestTemplateEntity> findByTestIdAndVariant(String testId, String variant);
    Optional<TestTemplateEntity> findByTestIdAndVariantAndPagePattern(String testId, String variant, String pagePattern);
    List<TestTemplateEntity> findByTestId(String testId);
    void deleteByTestId(String testId);
}
