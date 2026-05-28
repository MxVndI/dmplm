package com.diplom.testservice.domain.service;

import com.diplom.testservice.persistance.entity.TestArchiveEntity;
import com.diplom.testservice.persistance.entity.TestConfigEntity;
import com.diplom.testservice.persistance.entity.TestParticipantEntity;
import com.diplom.testservice.persistance.repository.TestArchiveRepository;
import com.diplom.testservice.persistance.repository.TestParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestArchiveService {

    private final TestArchiveRepository testArchiveRepository;
    private final TestParticipantRepository testParticipantRepository;

    public TestArchiveEntity archiveTest(TestConfigEntity test) {
        List<TestParticipantEntity> participants = testParticipantRepository.findByTestId(test.getId());

        long variantACount = participants.stream().filter(p -> "A".equals(p.getVariant())).count();
        long variantBCount = participants.stream().filter(p -> "B".equals(p.getVariant())).count();
        long totalParticipants = participants.size();

        long variantAConversions = (long) (variantACount * 0.1);
        long variantBConversions = (long) (variantBCount * 0.1);
        long totalConversions = variantAConversions + variantBConversions;

        double variantAConversionRate = variantACount > 0 ? (double) variantAConversions / variantACount : 0.0;
        double variantBConversionRate = variantBCount > 0 ? (double) variantBConversions / variantBCount : 0.0;
        double overallConversionRate = totalParticipants > 0 ? (double) totalConversions / totalParticipants : 0.0;

        double variantAAvgValuePerUser = 25.0;
        double variantBAvgValuePerUser = 27.0;
        double variantATotalValue = variantAAvgValuePerUser * variantACount;
        double variantBTotalValue = variantBAvgValuePerUser * variantBCount;

        long durationMinutes = test.getCreatedAt() != null && test.getCompletedAt() != null
                ? ChronoUnit.MINUTES.between(test.getCreatedAt(), test.getCompletedAt())
                : 0;

        TestArchiveEntity archive = TestArchiveEntity.builder()
                .testId(test.getId())
                .name(test.getName())
                .description(test.getDescription())
                .completedAt(test.getCompletedAt())
                .archivedAt(LocalDateTime.now())
                .durationMinutes(durationMinutes)
                .variantACount(variantACount)
                .variantAConversions(variantAConversions)
                .variantAConversionRate(variantAConversionRate)
                .variantAAvgValuePerUser(variantAAvgValuePerUser)
                .variantATotalValue(variantATotalValue)
                .variantBCount(variantBCount)
                .variantBConversions(variantBConversions)
                .variantBConversionRate(variantBConversionRate)
                .variantBAvgValuePerUser(variantBAvgValuePerUser)
                .variantBTotalValue(variantBTotalValue)
                .totalParticipants(totalParticipants)
                .totalConversions(totalConversions)
                .overallConversionRate(overallConversionRate)
                .build();

        TestArchiveEntity saved = testArchiveRepository.save(archive);
        log.info("Archived test '{}' (id={}): A={}, B={}, conversions={}, duration={}min",
                test.getName(), test.getId(), variantACount, variantBCount, totalConversions, durationMinutes);
        return saved;
    }

    public TestArchiveEntity getArchiveForTest(String testId) {
        return testArchiveRepository.findByTestId(testId).orElse(null);
    }

    public List<TestArchiveEntity> getAllArchives() {
        return testArchiveRepository.findAllByOrderByArchivedAtDesc();
    }
}
