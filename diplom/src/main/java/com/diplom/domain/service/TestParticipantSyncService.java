package com.diplom.domain.service;

import com.diplom.event.TestParticipantEvent;
import com.diplom.persistance.entity.UserTestParticipationEntity;
import com.diplom.persistance.repository.UserTestParticipationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestParticipantSyncService {

    private final UserTestParticipationRepository participationRepository;

    @KafkaListener(
            topics = "test-participants-result",
            groupId = "diplom-shop-group",
            containerFactory = "participantKafkaListenerContainerFactory"
    )
    public void onParticipantEnrolled(TestParticipantEvent event) {
        if (event == null || event.getTestId() == null || event.getUserId() == null) {
            log.warn("Received empty/null TestParticipantEvent, skipping.");
            return;
        }

        boolean alreadyEnrolled = participationRepository
                .findByTestIdAndUserId(event.getTestId(), event.getUserId())
                .isPresent();

        if (alreadyEnrolled) {
            log.debug("Participation already exists: testId={} userId={}", event.getTestId(), event.getUserId());
            return;
        }

        UserTestParticipationEntity p = new UserTestParticipationEntity();
        p.setTestId(event.getTestId());
        p.setUserId(event.getUserId());
        p.setVariant(event.getVariant());
        p.setEnrolledAt(event.getEnrolledAt() != null
                ? event.getEnrolledAt().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : java.time.LocalDateTime.now());

        participationRepository.save(p);
        log.info("Synced participant: userId={} → variant={} (testId={})",
                event.getUserId(), event.getVariant(), event.getTestId());
    }
}
