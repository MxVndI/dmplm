package com.diplom.mapper;

import com.diplom.domain.model.User;
import com.diplom.persistance.entity.UserEntity;
import com.diplom.rest.dto.UserRegistrationDto;
import com.diplom.rest.dto.UserUpdateDto;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-11T17:25:21+0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User restToDomain(UserRegistrationDto dto) {
        if ( dto == null ) {
            return null;
        }

        User user = new User();

        user.setAge( dto.getAge() );
        user.setCountry( dto.getCountry() );
        user.setFirstName( dto.getFirstName() );
        user.setGender( dto.getGender() );
        user.setLanguage( dto.getLanguage() );
        user.setLastName( dto.getLastName() );
        user.setPassword( dto.getPassword() );

        return user;
    }

    @Override
    public User restToDomainUpdate(UserUpdateDto dto) {
        if ( dto == null ) {
            return null;
        }

        User user = new User();

        user.setAge( dto.getAge() );
        user.setCountry( dto.getCountry() );
        user.setFirstName( dto.getFirstName() );
        user.setGender( dto.getGender() );
        user.setLanguage( dto.getLanguage() );
        user.setLastName( dto.getLastName() );
        user.setTelegramChatId( dto.getTelegramChatId() );

        return user;
    }

    @Override
    public User persistenceToDomain(UserEntity entity) {
        if ( entity == null ) {
            return null;
        }

        User user = new User();

        user.setAge( entity.getAge() );
        user.setCountry( entity.getCountry() );
        user.setCreatedAt( entity.getCreatedAt() );
        user.setFirstName( entity.getFirstName() );
        user.setGender( entity.getGender() );
        user.setId( entity.getId() );
        user.setLanguage( entity.getLanguage() );
        user.setLastName( entity.getLastName() );
        user.setPassword( entity.getPassword() );
        Set<String> set = entity.getRoles();
        if ( set != null ) {
            user.setRoles( new LinkedHashSet<String>( set ) );
        }
        user.setTelegramChatId( entity.getTelegramChatId() );

        return user;
    }

    @Override
    public UserEntity domainToPersistence(User domain) {
        if ( domain == null ) {
            return null;
        }

        UserEntity userEntity = new UserEntity();

        userEntity.setAge( domain.getAge() );
        userEntity.setCountry( domain.getCountry() );
        userEntity.setCreatedAt( domain.getCreatedAt() );
        userEntity.setFirstName( domain.getFirstName() );
        userEntity.setGender( domain.getGender() );
        userEntity.setId( domain.getId() );
        userEntity.setLanguage( domain.getLanguage() );
        userEntity.setLastName( domain.getLastName() );
        userEntity.setPassword( domain.getPassword() );
        Set<String> set = domain.getRoles();
        if ( set != null ) {
            userEntity.setRoles( new LinkedHashSet<String>( set ) );
        }
        userEntity.setTelegramChatId( domain.getTelegramChatId() );

        return userEntity;
    }
}
