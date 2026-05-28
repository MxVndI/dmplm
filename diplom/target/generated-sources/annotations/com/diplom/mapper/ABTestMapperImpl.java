package com.diplom.mapper;

import com.diplom.domain.model.ABTest;
import com.diplom.persistance.entity.ABTestEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-11T17:25:21+0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class ABTestMapperImpl implements ABTestMapper {

    @Override
    public ABTest persistenceToDomain(ABTestEntity entity) {
        if ( entity == null ) {
            return null;
        }

        ABTest aBTest = new ABTest();

        aBTest.setActive( entity.isActive() );
        aBTest.setCreatedAt( entity.getCreatedAt() );
        aBTest.setDescription( entity.getDescription() );
        aBTest.setEndedAt( entity.getEndedAt() );
        aBTest.setExpiresAt( entity.getExpiresAt() );
        aBTest.setId( entity.getId() );
        aBTest.setName( entity.getName() );

        return aBTest;
    }

    @Override
    public ABTestEntity domainToPersistence(ABTest domain) {
        if ( domain == null ) {
            return null;
        }

        ABTestEntity aBTestEntity = new ABTestEntity();

        aBTestEntity.setActive( domain.isActive() );
        aBTestEntity.setCreatedAt( domain.getCreatedAt() );
        aBTestEntity.setDescription( domain.getDescription() );
        aBTestEntity.setEndedAt( domain.getEndedAt() );
        aBTestEntity.setExpiresAt( domain.getExpiresAt() );
        aBTestEntity.setId( domain.getId() );
        aBTestEntity.setName( domain.getName() );

        return aBTestEntity;
    }
}
