package com.diplom.persistance.repository;

import com.diplom.persistance.entity.OrderEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<OrderEntity, String> {
    List<OrderEntity> findByUserId(String userId);
    List<OrderEntity> findByTestId(String testId);
    List<OrderEntity> findByTestIdAndVariant(String testId, String variant);
}
