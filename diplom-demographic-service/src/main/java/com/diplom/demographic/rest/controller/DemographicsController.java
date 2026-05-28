package com.diplom.demographic.rest.controller;

import com.diplom.demographic.persistance.entity.UserDemographicsEntity;
import com.diplom.demographic.domain.service.UserDemographicsEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demographics")
@RequiredArgsConstructor
public class DemographicsController {

    private final UserDemographicsEntityService service;

    @GetMapping
    public List<UserDemographicsEntity> all() {
        return service.findAll();
    }

    @GetMapping("/{userId}")
    public UserDemographicsEntity byUser(@PathVariable String userId) {
        return service.getByUserId(userId);
    }

    @PostMapping("/bulk")
    public List<UserDemographicsEntity> bulk(@RequestBody List<String> userIds) {
        return service.getByUserIds(userIds);
    }

    @PostMapping
    public ResponseEntity<UserDemographicsEntity> upsert(@RequestBody UserDemographicsEntity dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.upsert(dto));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> notFound(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }
}
