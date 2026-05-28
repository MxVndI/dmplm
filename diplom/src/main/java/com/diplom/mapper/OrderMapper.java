package com.diplom.mapper;

import com.diplom.domain.model.Order;
import com.diplom.persistance.entity.OrderEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    // Persistence to Domain
    Order persistenceToDomain(OrderEntity entity);

    // Domain to Persistence
    OrderEntity domainToPersistence(Order domain);
}
