package com.diplom.mapper;

import com.diplom.domain.model.TestTemplate;
import com.diplom.persistance.entity.TestTemplateEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TestTemplateMapper {
    TestTemplate persistenceToDomain(TestTemplateEntity entity);
    TestTemplateEntity domainToPersistence(TestTemplate domain);
}
