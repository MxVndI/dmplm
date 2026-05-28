package com.diplom.participant.rest.controller;

import com.diplom.participant.domain.service.ParticipantService;
import com.diplom.participant.persistance.entity.TestParticipantEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/participants")
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;

    @GetMapping("/test/{testId}")
    public List<TestParticipantEntity> byTest(@PathVariable String testId) {
        return participantService.listByTest(testId);
    }

    @GetMapping("/test/{testId}/user/{userId}")
    public TestParticipantEntity byTestAndUser(@PathVariable String testId, @PathVariable String userId) {
        return participantService.getByTestAndUser(testId, userId);
    }

    @GetMapping("/test/{testId}/distribution")
    public Map<String, Long> distribution(@PathVariable String testId) {
        return participantService.distribution(testId);
    }

    @DeleteMapping("/test/{testId}")
    public void clearByTest(@PathVariable String testId) {
        participantService.deleteByTestId(testId);
    }
}
