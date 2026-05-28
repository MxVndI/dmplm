package com.diplom.mapper;

import com.diplom.domain.model.ABTest;
import com.diplom.persistance.entity.ABTestEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ABTestMapper {

    // Persistence to Domain
    ABTest persistenceToDomain(ABTestEntity entity);

    // Domain to Persistence
    ABTestEntity domainToPersistence(ABTest domain);
}
