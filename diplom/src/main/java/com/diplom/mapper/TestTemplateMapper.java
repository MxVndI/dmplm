package com.diplom.mapper;

import com.diplom.domain.model.TestTemplate;
import com.diplom.persistance.entity.TestTemplateEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TestTemplateMapper {

    // Persistence to Domain
    TestTemplate persistenceToDomain(TestTemplateEntity entity);

    // Domain to Persistence
    TestTemplateEntity domainToPersistence(TestTemplate domain);
}
