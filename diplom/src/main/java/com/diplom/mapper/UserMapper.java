package com.diplom.mapper;

import com.diplom.domain.model.User;
import com.diplom.persistance.entity.UserEntity;
import com.diplom.rest.dto.UserRegistrationDto;
import com.diplom.rest.dto.UserUpdateDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "blocked", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "telegramChatId", ignore = true)
    UserEntity toEntity(UserRegistrationDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "login", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "blocked", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "telegramChatId", ignore = true)
    void updateEntityFromDto(UserUpdateDto dto, @MappingTarget UserEntity entity);

    UserUpdateDto toUpdateDto(UserEntity entity);

    User restToDomain(UserRegistrationDto dto);

    User restToDomainUpdate(UserUpdateDto dto);

    User persistenceToDomain(UserEntity entity);

    UserEntity domainToPersistence(User domain);
}
