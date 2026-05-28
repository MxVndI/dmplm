package com.diplom.testservice.persistance.repository;

import com.diplom.testservice.persistance.entity.ABRuleEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ABRuleRepository extends MongoRepository<ABRuleEntity, String> {

    List<ABRuleEntity> findByActiveTrue();
}
