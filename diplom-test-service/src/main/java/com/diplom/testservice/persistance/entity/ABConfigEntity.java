package com.diplom.testservice.persistance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@Document(collection = "ab_configs")
public class ABConfigEntity {

    @Id
    private String id;

    private String name;
    private String description;

    private Map<String, Integer> variants;

    private boolean active = true;
    private Instant createdAt;
}
