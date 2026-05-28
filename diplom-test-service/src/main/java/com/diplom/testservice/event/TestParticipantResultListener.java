package com.diplom.testservice.event;

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
            topics = "test-participants-result",
            groupId = "diplom-test-service-group",
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
                event.getEnrolledAt()
        );
    }
}
