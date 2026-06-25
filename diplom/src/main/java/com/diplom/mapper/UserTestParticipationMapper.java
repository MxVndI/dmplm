package com.diplom.mapper;

import com.diplom.domain.model.UserTestParticipation;
import com.diplom.persistance.entity.UserTestParticipationEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserTestParticipationMapper {
    UserTestParticipation persistenceToDomain(UserTestParticipationEntity entity);
    UserTestParticipationEntity domainToPersistence(UserTestParticipation domain);
}
