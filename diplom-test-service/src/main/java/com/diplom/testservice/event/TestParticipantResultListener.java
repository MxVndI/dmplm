package com.diplom.testservice.event;

import com.diplom.testservice.constant.AppConstants;
import com.diplom.testservice.domain.service.TestConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestParticipantResultListener {

    private final TestConfigService testConfigService;

    @KafkaListener(
            topics = AppConstants.TOPIC_PARTICIPANTS_RESULT,
            groupId = AppConstants.TEST_SERVICE_GROUP_ID,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onParticipantResult(TestParticipantEvent event) {
        if (event == null || event.getTestId() == null || event.getUserId() == null) {
            log.warn("Skipping invalid participant event: {}", event);
            return;
        }
        testConfigService.onParticipantResult(
                event.getTestId(),
                event.getUserId(),
                event.getVariant(),
                event.getClusterId(),
                event.getEnrolledAt()
        );
    }
}
