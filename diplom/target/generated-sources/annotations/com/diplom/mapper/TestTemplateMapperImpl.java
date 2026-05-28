package com.diplom.mapper;

import com.diplom.domain.model.TestTemplate;
import com.diplom.persistance.entity.TestTemplateEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-11T17:25:21+0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class TestTemplateMapperImpl implements TestTemplateMapper {

    @Override
    public TestTemplate persistenceToDomain(TestTemplateEntity entity) {
        if ( entity == null ) {
            return null;
        }

        TestTemplate testTemplate = new TestTemplate();

        testTemplate.setId( entity.getId() );
        testTemplate.setMinioKey( entity.getMinioKey() );
        testTemplate.setName( entity.getName() );
        testTemplate.setOriginalFileName( entity.getOriginalFileName() );
        testTemplate.setTestId( entity.getTestId() );
        testTemplate.setUploadedAt( entity.getUploadedAt() );
        testTemplate.setVariant( entity.getVariant() );

        return testTemplate;
    }

    @Override
    public TestTemplateEntity domainToPersistence(TestTemplate domain) {
        if ( domain == null ) {
            return null;
        }

        TestTemplateEntity testTemplateEntity = new TestTemplateEntity();

        testTemplateEntity.setId( domain.getId() );
        testTemplateEntity.setMinioKey( domain.getMinioKey() );
        testTemplateEntity.setName( domain.getName() );
        testTemplateEntity.setOriginalFileName( domain.getOriginalFileName() );
        testTemplateEntity.setTestId( domain.getTestId() );
        testTemplateEntity.setUploadedAt( domain.getUploadedAt() );
        testTemplateEntity.setVariant( domain.getVariant() );

        return testTemplateEntity;
    }
}
