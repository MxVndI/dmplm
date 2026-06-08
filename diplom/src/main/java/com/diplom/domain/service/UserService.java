package com.diplom.domain.service;

import com.diplom.event.UserProfileEvent;
import com.diplom.mapper.UserMapper;
import com.diplom.persistance.entity.UserEntity;
import com.diplom.persistance.repository.UserRepository;
import com.diplom.rest.dto.UserRegistrationDto;
import com.diplom.rest.dto.UserUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, UserProfileEvent> kafkaTemplate;
    @Qualifier("eventKafkaTemplate")
    private final KafkaTemplate<String, Object> eventKafkaTemplate;
    private final UserMapper userMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.demographic-service-url:http://localhost:8084}")
    private String demographicServiceUrl;

    public UserEntity register(UserRegistrationDto dto) {
        UserEntity user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRoles(Set.of("ROLE_USER"));
        user.setBlocked(false);
        user.setCreatedAt(LocalDateTime.now());

        try {
            UserEntity saved = userRepository.save(user);
            publishProfile(saved);
            syncDemographics(saved, dto);
            return saved;
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("Login or email is already taken.");
        }
    }

    private void syncDemographics(UserEntity user, UserRegistrationDto dto) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("userId", user.getId());
            if (dto.getIncomeLevel() != null && !dto.getIncomeLevel().isBlank()) {
                body.put("incomeLevel", dto.getIncomeLevel());
            }
            if (dto.getEducationLevel() != null && !dto.getEducationLevel().isBlank()) {
                body.put("educationLevel", dto.getEducationLevel());
            }
            if (dto.getOccupation() != null && !dto.getOccupation().isBlank()) {
                body.put("occupation", dto.getOccupation());
            }
            if (dto.getInterestsRaw() != null && !dto.getInterestsRaw().isBlank()) {
                List<String> interests = Arrays.stream(dto.getInterestsRaw().split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                body.put("interests", interests);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(demographicServiceUrl + "/api/demographics", request, String.class);
            log.debug("Demographics record synced for userId={}", user.getId());
        } catch (Exception e) {
            log.warn("Could not sync demographics for userId={}: {}", user.getId(), e.getMessage());
        }
    }

    public void publishProfile(UserEntity user) {
        UserProfileEvent event = new UserProfileEvent(
                user.getId(),
                user.getAge(),
                user.getCountry(),
                user.getLanguage(),
                user.getGender() != null ? user.getGender().name() : null
        );
        kafkaTemplate.send("user-profiles", user.getId(), event);
        log.debug("Published user-profile event for user={}", user.getId());
        publishLoginEvent(user.getId());
    }

    private void publishLoginEvent(String userId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("eventType", "LOGIN");
            payload.put("timestamp", System.currentTimeMillis());
            eventKafkaTemplate.send("user-events", userId, payload);
        } catch (Exception e) {
            log.warn("Failed to publish LOGIN event for user={}: {}", userId, e.getMessage());
        }
    }

    public Optional<UserEntity> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public Optional<UserEntity> findById(String id) {
        return userRepository.findById(id);
    }

    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    public UserEntity update(String userId, UserUpdateDto dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userMapper.updateEntityFromDto(dto, user);
        user.setTelegramChatId(dto.getTelegramChatId() != null && !dto.getTelegramChatId().isBlank()
                ? dto.getTelegramChatId() : null);

        try {
            UserEntity saved = userRepository.save(user);
            publishProfile(saved);
            syncDemographicsFromUpdate(saved, dto);
            return saved;
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("Email is already taken by another account.");
        }
    }

    private void syncDemographicsFromUpdate(UserEntity user, UserUpdateDto dto) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("userId", user.getId());
            if (user.getAge() != null) body.put("age", user.getAge());
            if (user.getCountry() != null && !user.getCountry().isBlank()) body.put("country", user.getCountry());
            if (user.getLanguage() != null && !user.getLanguage().isBlank()) body.put("language", user.getLanguage());
            if (user.getGender() != null) body.put("gender", user.getGender().name());
            if (dto.getIncomeLevel() != null && !dto.getIncomeLevel().isBlank())
                body.put("incomeLevel", dto.getIncomeLevel());
            if (dto.getEducationLevel() != null && !dto.getEducationLevel().isBlank())
                body.put("educationLevel", dto.getEducationLevel());
            if (dto.getOccupation() != null && !dto.getOccupation().isBlank())
                body.put("occupation", dto.getOccupation());
            if (dto.getInterestsRaw() != null && !dto.getInterestsRaw().isBlank()) {
                List<String> interests = Arrays.stream(dto.getInterestsRaw().split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                body.put("interests", interests);
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(demographicServiceUrl + "/api/demographics", request, String.class);
            log.debug("Demographics synced on profile update for userId={}", user.getId());
        } catch (Exception e) {
            log.warn("Could not sync demographics on update for userId={}: {}", user.getId(), e.getMessage());
        }
    }

    public UserEntity setBlocked(String userId, boolean blocked) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setBlocked(blocked);
        return userRepository.save(user);
    }
}
