package com.diplom.testservice.persistance.repository;

import com.diplom.testservice.persistance.entity.TestArchiveEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestArchiveRepository extends MongoRepository<TestArchiveEntity, String> {
    Optional<TestArchiveEntity> findByTestId(String testId);
    List<TestArchiveEntity> findAllByOrderByArchivedAtDesc();
}
