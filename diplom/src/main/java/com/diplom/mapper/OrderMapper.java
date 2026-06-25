package com.diplom.mapper;

import com.diplom.domain.model.Order;
import com.diplom.persistance.entity.OrderEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order persistenceToDomain(OrderEntity entity);
    OrderEntity domainToPersistence(Order domain);
}
