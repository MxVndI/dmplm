package com.diplom.participant.domain.service;

import com.diplom.participant.persistance.entity.TestParticipantEntity;
import com.diplom.participant.persistance.repository.TestParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final TestParticipantRepository participantRepository;

    public void saveIfAbsent(String testId, String userId, String variant, Instant enrolledAt) {
        if (participantRepository.findByTestIdAndUserId(testId, userId).isPresent()) {
            return;
        }
        participantRepository.save(new TestParticipantEntity(
                null,
                testId,
                userId,
                variant,
                enrolledAt == null ? Instant.now() : enrolledAt
        ));
    }

    public List<TestParticipantEntity> listByTest(String testId) {
        return participantRepository.findByTestId(testId);
    }

    public TestParticipantEntity getByTestAndUser(String testId, String userId) {
        return participantRepository.findByTestIdAndUserId(testId, userId)
                .orElse(null);
    }

    public Map<String, Long> distribution(String testId) {
        long total = participantRepository.countByTestId(testId);
        long variantA = participantRepository.countByTestIdAndVariant(testId, "A");
        long variantB = participantRepository.countByTestIdAndVariant(testId, "B");
        return Map.of("total", total, "variantA", variantA, "variantB", variantB);
    }

    public void deleteByTestId(String testId) {
        participantRepository.deleteByTestId(testId);
    }
}
