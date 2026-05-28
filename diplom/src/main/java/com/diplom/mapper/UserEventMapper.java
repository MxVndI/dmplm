package com.diplom.mapper;

import com.diplom.domain.model.UserEvent;
import com.diplom.persistance.entity.UserEventEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserEventMapper {

    // Persistence to Domain
    UserEvent persistenceToDomain(UserEventEntity entity);

    // Domain to Persistence
    UserEventEntity domainToPersistence(UserEvent domain);
}
