package com.diplom.testservice.mapper;

import com.diplom.testservice.persistance.entity.ABConfigEntity;
import com.diplom.testservice.persistance.entity.ABRuleEntity;
import com.diplom.testservice.rest.dto.CreateABConfigDto;
import com.diplom.testservice.rest.dto.CreateABRuleDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ABRuleMapper {

    @Mapping(target = "id", ignore = true)
    ABRuleEntity toRuleEntity(CreateABRuleDto dto);

    @Mapping(target = "createdAt", ignore = true)
    ABConfigEntity toConfigEntity(CreateABConfigDto dto);
}
