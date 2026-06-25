package com.diplom.mapper;

import com.diplom.domain.model.UserEvent;
import com.diplom.persistance.entity.UserEventEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserEventMapper {
    UserEvent persistenceToDomain(UserEventEntity entity);
    UserEventEntity domainToPersistence(UserEvent domain);
}
