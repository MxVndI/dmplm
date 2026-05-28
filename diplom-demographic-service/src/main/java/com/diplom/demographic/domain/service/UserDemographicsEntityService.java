package com.diplom.demographic.domain.service;

import com.diplom.demographic.persistance.entity.UserDemographicsEntity;
import com.diplom.demographic.persistance.repository.UserDemographicsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDemographicsEntityService {

    private final UserDemographicsRepository repository;

    public List<UserDemographicsEntity> findAll() {
        return repository.findAll();
    }

    public UserDemographicsEntity getByUserId(String userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Demographics not found for userId=" + userId));
    }

    public List<UserDemographicsEntity> getByUserIds(List<String> userIds) {
        return repository.findByUserIdIn(userIds);
    }

    public UserDemographicsEntity upsert(UserDemographicsEntity incoming) {
        UserDemographicsEntity e = repository.findByUserId(incoming.getUserId()).orElse(null);
        if (e == null) {
            incoming.setCreatedAt(LocalDateTime.now());
            incoming.setUpdatedAt(LocalDateTime.now());
            return repository.save(incoming);
        }
        e.setIncomeLevel(incoming.getIncomeLevel());
        e.setEducationLevel(incoming.getEducationLevel());
        e.setOccupation(incoming.getOccupation());
        e.setInterests(incoming.getInterests());
        e.setUpdatedAt(LocalDateTime.now());
        return repository.save(e);
    }
}
