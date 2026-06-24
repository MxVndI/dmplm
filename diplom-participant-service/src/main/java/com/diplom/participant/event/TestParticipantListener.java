package com.diplom.participant.event;

import com.diplom.participant.constant.AppConstants;
import com.diplom.participant.domain.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestParticipantListener {

    private final ParticipantService participantService;

    @KafkaListener(
            topics = AppConstants.TOPIC_PARTICIPANTS_RESULT,
            groupId = AppConstants.PARTICIPANT_GROUP_ID,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onResult(TestParticipantEvent event) {
        if (event == null || event.getTestId() == null || event.getUserId() == null) {
            log.warn("Skipping invalid participant event: {}", event);
            return;
        }
        participantService.saveIfAbsent(
                event.getTestId(),
                event.getUserId(),
                event.getVariant(),
                event.getClusterId(),
                event.getEnrolledAt()
        );
    }
}
