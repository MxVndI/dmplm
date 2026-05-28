package com.diplom.demographic.persistance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "user_demographics")
public class UserDemographicsEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String incomeLevel;

    private String educationLevel;

    private String occupation;

    private List<String> interests;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
