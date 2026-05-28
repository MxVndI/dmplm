package com.diplom.persistance.repository;

import com.diplom.persistance.entity.UserTestParticipationEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTestParticipationRepository extends MongoRepository<UserTestParticipationEntity, String> {
    Optional<UserTestParticipationEntity> findByTestIdAndUserId(String testId, String userId);

    List<UserTestParticipationEntity> findByUserId(String userId);

    List<UserTestParticipationEntity> findByTestId(String testId);

    void deleteByTestId(String testId);
}
