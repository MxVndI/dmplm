package com.diplom.mapper;

import com.diplom.domain.model.User;
import com.diplom.persistance.entity.UserEntity;
import com.diplom.rest.dto.UserRegistrationDto;
import com.diplom.rest.dto.UserUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // ── DTO ↔ Entity (direct) ─────────────────────────────────────────────
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

    /** Snapshot the editable parts of a user into a fresh DTO (for the edit form). */
    UserUpdateDto toUpdateDto(UserEntity entity);

    // ── Legacy domain-layer mappings (kept for backwards compatibility) ───
    User restToDomain(UserRegistrationDto dto);
    User restToDomainUpdate(UserUpdateDto dto);
    User persistenceToDomain(UserEntity entity);
    UserEntity domainToPersistence(User domain);
}
