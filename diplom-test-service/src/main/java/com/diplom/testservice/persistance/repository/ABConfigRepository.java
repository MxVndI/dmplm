package com.diplom.testservice.persistance.repository;

import com.diplom.testservice.persistance.entity.ABConfigEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ABConfigRepository extends MongoRepository<ABConfigEntity, String> {

    List<ABConfigEntity> findByActiveTrue();
}
