package com.diplom.mapper;

import com.diplom.domain.model.ABTest;
import com.diplom.persistance.entity.ABTestEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ABTestMapper {
    ABTest persistenceToDomain(ABTestEntity entity);
    ABTestEntity domainToPersistence(ABTest domain);
}
