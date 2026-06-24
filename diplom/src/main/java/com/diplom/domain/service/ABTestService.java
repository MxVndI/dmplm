package com.diplom.domain.service;

import com.diplom.constant.AppConstants;
import com.diplom.persistance.entity.ABTestEntity;
import com.diplom.persistance.entity.UserTestParticipationEntity;
import com.diplom.persistance.repository.ABTestRepository;
import com.diplom.persistance.repository.UserTestParticipationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j

@Service
@RequiredArgsConstructor
public class ABTestService {

    private final ABTestRepository abTestRepository;
    private final UserTestParticipationRepository participationRepository;

    public List<ABTestEntity> findAllTests() {
        return abTestRepository.findAll();
    }

    public ABTestEntity findTestById(String id) {
        return abTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.AB_TEST_NOT_FOUND + id));
    }

    public ABTestEntity createTest(String name, String description, LocalDateTime expiresAt) {
        ABTestEntity test = new ABTestEntity();
        test.setName(name);
        test.setDescription(description);
        test.setActive(true);
        test.setCreatedAt(LocalDateTime.now());
        test.setExpiresAt(expiresAt);
        return abTestRepository.save(test);
    }

    public void deactivateTest(String testId) {
        ABTestEntity test = findTestById(testId);
        test.setActive(false);
        test.setEndedAt(LocalDateTime.now());
        abTestRepository.save(test);
        participationRepository.deleteByTestId(testId);
        log.info("Test {} deactivated; all participant records removed.", testId);
    }

    public void deleteTest(String testId) {
        participationRepository.deleteByTestId(testId);
        abTestRepository.deleteById(testId);
        log.info("Test {} deleted along with all participant records.", testId);
    }

    @Scheduled(fixedDelay = 60_000)
    public void deactivateExpiredTests() {
        List<ABTestEntity> expired = abTestRepository.findByActive(true).stream()
                .filter(t -> t.getExpiresAt() != null && t.getExpiresAt().isBefore(LocalDateTime.now()))
                .toList();
        for (ABTestEntity t : expired) {
            log.info("Auto-deactivating expired test '{}' (id={})", t.getName(), t.getId());
            deactivateTest(t.getId());
        }
    }

    public UserTestParticipationEntity enrollUser(String testId, String userId, String variant) {
        if (participationRepository.findByTestIdAndUserId(testId, userId).isPresent()) {
            throw new IllegalArgumentException(AppConstants.USER_ALREADY_ENROLLED);
        }

        ABTestEntity test = findTestById(testId);
        if (!test.isActive()) {
            throw new IllegalArgumentException(AppConstants.CANNOT_ENROLL_IN_INACTIVE_TEST);
        }

        UserTestParticipationEntity participation = new UserTestParticipationEntity();
        participation.setTestId(testId);
        participation.setUserId(userId);
        participation.setVariant((variant != null && !variant.isBlank()) ? variant : randomVariant());
        participation.setEnrolledAt(LocalDateTime.now());
        return participationRepository.save(participation);
    }

    public void removeParticipation(String participationId) {
        participationRepository.deleteById(participationId);
    }

    public Optional<UserTestParticipationEntity> findActiveParticipation(String userId) {
        List<ABTestEntity> activeTests = abTestRepository.findByActive(true).stream()
                .filter(t -> t.getExpiresAt() == null || t.getExpiresAt().isAfter(LocalDateTime.now()))
                .toList();
        List<UserTestParticipationEntity> userParticipations = participationRepository.findByUserId(userId);

        Optional<UserTestParticipationEntity> fromOldSystem = userParticipations.stream()
                .filter(p -> activeTests.stream().anyMatch(t -> t.getId().equals(p.getTestId())))
                .findFirst();
        if (fromOldSystem.isPresent()) return fromOldSystem;

        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        return userParticipations.stream()
                .filter(p -> p.getEnrolledAt() != null && p.getEnrolledAt().isAfter(cutoff))
                .filter(p -> activeTests.stream().noneMatch(t -> t.getId().equals(p.getTestId())))
                .max(Comparator.comparing(UserTestParticipationEntity::getEnrolledAt));
    }

    public List<UserTestParticipationEntity> findParticipationsByTest(String testId) {
        return participationRepository.findByTestId(testId);
    }

    private String randomVariant() {
        return ThreadLocalRandom.current().nextBoolean() ? AppConstants.VARIANT_A : AppConstants.VARIANT_B;
    }
}
