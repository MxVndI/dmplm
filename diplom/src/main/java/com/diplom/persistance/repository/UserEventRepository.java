package com.diplom.persistance.repository;

import com.diplom.persistance.entity.UserEventEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for user behaviour metrics.
 * Extended query methods will be added when metrics collection is implemented.
 */
@Repository

public interface UserEventRepository extends MongoRepository<UserEventEntity, String> {
    List<UserEventEntity> findByUserId(String userId);
    List<UserEventEntity> findByTestId(String testId);
    List<UserEventEntity> findByTimestampBetween(LocalDateTime from, LocalDateTime to);
    List<UserEventEntity> findByTestIdAndVariant(String testId, String variant);
}
