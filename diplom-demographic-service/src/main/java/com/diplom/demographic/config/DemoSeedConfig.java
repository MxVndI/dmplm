package com.diplom.demographic.config;

import com.diplom.demographic.persistance.entity.UserDemographicsEntity;
import com.diplom.demographic.persistance.repository.UserDemographicsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DemoSeedConfig {

    private final UserDemographicsRepository repository;

    @Bean
    CommandLineRunner seedDemoData() {
        return args -> {
            if (repository.count() > 0) return;

            repository.saveAll(List.of(
                    record("demo-user-1", "LOW", "SECONDARY", "STUDENT", List.of("gaming", "music")),
                    record("demo-user-2", "MEDIUM", "HIGHER", "EMPLOYED", List.of("technology", "travel")),
                    record("demo-user-3", "HIGH", "ACADEMIC", "SELF_EMPLOYED", List.of("business", "finance"))
            ));
        };
    }

    private UserDemographicsEntity record(String userId, String income, String education,
                                    String occupation, List<String> interests) {
        UserDemographicsEntity d = new UserDemographicsEntity();
        d.setUserId(userId);
        d.setIncomeLevel(income);
        d.setEducationLevel(education);
        d.setOccupation(occupation);
        d.setInterests(interests);
        d.setCreatedAt(LocalDateTime.now());
        d.setUpdatedAt(LocalDateTime.now());
        return d;
    }
}
