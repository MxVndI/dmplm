package com.diplom.testservice.mapper;

import com.diplom.testservice.persistance.entity.TestConfigEntity;
import com.diplom.testservice.rest.dto.CreateTestDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface TestConfigMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "triggeredAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "enrolledCount", ignore = true)
    TestConfigEntity toEntity(CreateTestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "triggeredAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "enrolledCount", ignore = true)
    void updateEntityFromDto(CreateTestDto dto, @MappingTarget TestConfigEntity entity);
}
