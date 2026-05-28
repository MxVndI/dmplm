package com.diplom.mapper;

import com.diplom.domain.model.UserEvent;
import com.diplom.persistance.entity.UserEventEntity;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-11T17:25:21+0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UserEventMapperImpl implements UserEventMapper {

    @Override
    public UserEvent persistenceToDomain(UserEventEntity entity) {
        if ( entity == null ) {
            return null;
        }

        UserEvent userEvent = new UserEvent();

        Map<String, Object> map = entity.getEventData();
        if ( map != null ) {
            userEvent.setEventData( new LinkedHashMap<String, Object>( map ) );
        }
        userEvent.setEventType( entity.getEventType() );
        userEvent.setId( entity.getId() );
        userEvent.setIpAddress( entity.getIpAddress() );
        userEvent.setPage( entity.getPage() );
        userEvent.setSessionId( entity.getSessionId() );
        userEvent.setTestId( entity.getTestId() );
        userEvent.setTimestamp( entity.getTimestamp() );
        userEvent.setUserAgent( entity.getUserAgent() );
        userEvent.setUserId( entity.getUserId() );
        userEvent.setVariant( entity.getVariant() );

        return userEvent;
    }

    @Override
    public UserEventEntity domainToPersistence(UserEvent domain) {
        if ( domain == null ) {
            return null;
        }

        UserEventEntity userEventEntity = new UserEventEntity();

        Map<String, Object> map = domain.getEventData();
        if ( map != null ) {
            userEventEntity.setEventData( new LinkedHashMap<String, Object>( map ) );
        }
        userEventEntity.setEventType( domain.getEventType() );
        userEventEntity.setId( domain.getId() );
        userEventEntity.setIpAddress( domain.getIpAddress() );
        userEventEntity.setPage( domain.getPage() );
        userEventEntity.setSessionId( domain.getSessionId() );
        userEventEntity.setTestId( domain.getTestId() );
        userEventEntity.setTimestamp( domain.getTimestamp() );
        userEventEntity.setUserAgent( domain.getUserAgent() );
        userEventEntity.setUserId( domain.getUserId() );
        userEventEntity.setVariant( domain.getVariant() );

        return userEventEntity;
    }
}
