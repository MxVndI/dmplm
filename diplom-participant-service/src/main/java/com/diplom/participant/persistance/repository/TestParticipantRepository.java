package com.diplom.participant.persistance.repository;

import com.diplom.participant.persistance.entity.TestParticipantEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TestParticipantRepository extends MongoRepository<TestParticipantEntity, String> {
    List<TestParticipantEntity> findByTestId(String testId);
    Optional<TestParticipantEntity> findByTestIdAndUserId(String testId, String userId);
    long countByTestId(String testId);
    long countByTestIdAndVariant(String testId, String variant);
    long countByTestIdAndClusterIdAndVariant(String testId, Integer clusterId, String variant);
    void deleteByTestId(String testId);
}
