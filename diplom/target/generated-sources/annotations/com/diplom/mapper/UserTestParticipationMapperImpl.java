package com.diplom.mapper;

import com.diplom.domain.model.UserTestParticipation;
import com.diplom.persistance.entity.UserTestParticipationEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-11T17:25:21+0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UserTestParticipationMapperImpl implements UserTestParticipationMapper {

    @Override
    public UserTestParticipation persistenceToDomain(UserTestParticipationEntity entity) {
        if ( entity == null ) {
            return null;
        }

        UserTestParticipation userTestParticipation = new UserTestParticipation();

        userTestParticipation.setEnrolledAt( entity.getEnrolledAt() );
        userTestParticipation.setId( entity.getId() );
        userTestParticipation.setTestId( entity.getTestId() );
        userTestParticipation.setUserId( entity.getUserId() );
        userTestParticipation.setVariant( entity.getVariant() );

        return userTestParticipation;
    }

    @Override
    public UserTestParticipationEntity domainToPersistence(UserTestParticipation domain) {
        if ( domain == null ) {
            return null;
        }

        UserTestParticipationEntity userTestParticipationEntity = new UserTestParticipationEntity();

        userTestParticipationEntity.setEnrolledAt( domain.getEnrolledAt() );
        userTestParticipationEntity.setId( domain.getId() );
        userTestParticipationEntity.setTestId( domain.getTestId() );
        userTestParticipationEntity.setUserId( domain.getUserId() );
        userTestParticipationEntity.setVariant( domain.getVariant() );

        return userTestParticipationEntity;
    }
}
