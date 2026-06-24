package com.diplom.testservice.domain.service;

import com.diplom.testservice.constant.AppConstants;
import com.diplom.testservice.mapper.TestConfigMapper;
import com.diplom.testservice.rest.dto.CreateTestDto;
import com.diplom.testservice.event.SelectionRequest;
import com.diplom.testservice.persistance.entity.TestArchiveEntity;
import com.diplom.testservice.persistance.entity.TestConfigEntity;
import com.diplom.testservice.persistance.entity.TestParticipantEntity;
import com.diplom.testservice.persistance.entity.TestStatus;
import com.diplom.testservice.persistance.repository.TestConfigRepository;
import com.diplom.testservice.persistance.repository.TestParticipantRepository;
import com.diplom.testservice.rest.client.NotificationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestConfigService {

    private final TestConfigRepository testConfigRepository;
    private final TestParticipantRepository testParticipantRepository;
    private final KafkaTemplate<String, SelectionRequest> kafkaTemplate;
    private final TestConfigMapper testConfigMapper;
    private final TestArchiveService testArchiveService;
    private final NotificationClient notificationClient;

    public TestConfigEntity create(CreateTestDto dto) {
        TestConfigEntity test = testConfigMapper.toEntity(dto);
        test.setStatus(TestStatus.DRAFT);
        test.setCreatedAt(LocalDateTime.now());
        return testConfigRepository.save(test);
    }

    public List<TestConfigEntity> findAll() {
        return testConfigRepository.findAll();
    }

    public TestConfigEntity findById(String id) {
        return testConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.TEST_NOT_FOUND + id));
    }

    public TestConfigEntity triggerSelection(String testId) {
        TestConfigEntity test = findById(testId);
        if (test.getStatus() == TestStatus.COMPLETED) {
            throw new IllegalStateException(AppConstants.COMPLETED_TEST_SELECTION_FORBIDDEN);
        }

        SelectionRequest request = new SelectionRequest(testId, test.getName(), test.getCriteria());
        kafkaTemplate.send(AppConstants.TOPIC_SELECTION_REQUESTS, testId, request);

        test.setStatus(TestStatus.RUNNING);
        test.setTriggeredAt(LocalDateTime.now());
        testConfigRepository.save(test);
        log.info("Selection triggered for test '{}' (id={})", test.getName(), testId);
        return test;
    }

    public TestConfigEntity activate(String testId) {
        TestConfigEntity test = findById(testId);
        test.setStatus(TestStatus.ACTIVE);
        return testConfigRepository.save(test);
    }

    public TestConfigEntity complete(String testId) {
        TestConfigEntity test = findById(testId);
        test.setStatus(TestStatus.COMPLETED);
        test.setCompletedAt(LocalDateTime.now());
        return testConfigRepository.save(test);
    }

    public List<TestParticipantEntity> getParticipants(String testId) {
        return testParticipantRepository.findByTestId(testId);
    }

    public Map<String, Long> getStats(String testId) {
        long total = testParticipantRepository.countByTestId(testId);
        long variantA = testParticipantRepository.countByTestIdAndVariant(testId, AppConstants.VARIANT_A);
        long variantB = testParticipantRepository.countByTestIdAndVariant(testId, AppConstants.VARIANT_B);
        return Map.of("total", total, "variantA", variantA, "variantB", variantB);
    }

    public TestConfigEntity update(String testId, CreateTestDto dto) {
        TestConfigEntity test = findById(testId);
        if (test.getStatus() == TestStatus.COMPLETED) {
            throw new IllegalStateException(AppConstants.COMPLETED_TEST_EDIT_FORBIDDEN);
        }
        testConfigMapper.updateEntityFromDto(dto, test);
        return testConfigRepository.save(test);
    }

    public TestConfigEntity restart(String testId) {
        TestConfigEntity test = findById(testId);
        clearParticipantsByTest(testId);
        test.setStatus(TestStatus.DRAFT);
        test.setEnrolledCount(0);
        test.setTriggeredAt(null);
        testConfigRepository.save(test);
        log.info("Restarting test '{}' (id={})", test.getName(), testId);
        return triggerSelection(testId);
    }

    public void delete(String testId) {
        TestConfigEntity test = findById(testId);
        clearParticipantsByTest(testId);
        testConfigRepository.delete(test);
        log.info("Deleted test '{}' (id={})", test.getName(), testId);
    }

    public void onParticipantResult(String testId, String userId, String variant, Integer clusterId, java.time.Instant enrolledAt) {
        boolean alreadyEnrolled = testParticipantRepository.findByTestIdAndUserId(testId, userId).isPresent();
        if (!alreadyEnrolled) {
            TestParticipantEntity p = new TestParticipantEntity();
            p.setTestId(testId);
            p.setUserId(userId);
            p.setVariant(variant);
            p.setClusterId(clusterId);
            p.setEnrolledAt(enrolledAt != null ? enrolledAt : java.time.Instant.now());
            testParticipantRepository.save(p);
            log.debug("Saved participant: userId={} variant={} testId={}", userId, variant, testId);
        }

        testConfigRepository.findById(testId).ifPresent(test -> {
            test.setEnrolledCount((int) getStats(testId).getOrDefault("total", 0L).longValue());
            if (test.getStatus() == TestStatus.RUNNING) {
                test.setStatus(TestStatus.ACTIVE);
            }
            testConfigRepository.save(test);
        });
    }

    private void clearParticipantsByTest(String testId) {
        testParticipantRepository.deleteByTestId(testId);
    }

    @Scheduled(fixedDelay = 60_000)
    public void autoCompleteExpiredTests() {
        testConfigRepository.findByStatus(TestStatus.ACTIVE).forEach(test -> {
            if (test.getExpiresAt() != null && test.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.info("Auto-completing expired test '{}' (id={})", test.getName(), test.getId());

                test.setStatus(TestStatus.COMPLETED);
                test.setCompletedAt(LocalDateTime.now());
                testConfigRepository.save(test);

                TestArchiveEntity archive = testArchiveService.archiveTest(test);

                notificationClient.notifyTestExpired(archive);

                clearParticipantsByTest(test.getId());
            }
        });
    }
}
