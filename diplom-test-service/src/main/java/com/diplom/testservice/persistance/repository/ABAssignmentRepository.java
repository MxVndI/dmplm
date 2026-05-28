package com.diplom.testservice.persistance.repository;

import com.diplom.testservice.persistance.entity.ABAssignmentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ABAssignmentRepository extends MongoRepository<ABAssignmentEntity, String> {

    Optional<ABAssignmentEntity> findByUserIdAndAbTestId(String userId, String abTestId);

    List<ABAssignmentEntity> findByUserId(String userId);

    void deleteByUserIdAndAbTestId(String userId, String abTestId);
}
