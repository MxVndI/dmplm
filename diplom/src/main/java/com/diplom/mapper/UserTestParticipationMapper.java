package com.diplom.mapper;

import com.diplom.domain.model.UserTestParticipation;
import com.diplom.persistance.entity.UserTestParticipationEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserTestParticipationMapper {

    // Persistence to Domain
    UserTestParticipation persistenceToDomain(UserTestParticipationEntity entity);

    // Domain to Persistence
    UserTestParticipationEntity domainToPersistence(UserTestParticipation domain);
}
