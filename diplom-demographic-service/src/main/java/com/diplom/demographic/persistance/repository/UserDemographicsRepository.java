package com.diplom.demographic.persistance.repository;

import com.diplom.demographic.persistance.entity.UserDemographicsEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserDemographicsRepository extends MongoRepository<UserDemographicsEntity, String> {
    Optional<UserDemographicsEntity> findByUserId(String userId);
    List<UserDemographicsEntity> findByUserIdIn(List<String> userIds);
}
