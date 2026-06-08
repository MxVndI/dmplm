package com.diplom.persistance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@Document(collection = "users")
public class UserEntity {

    @Id
    private String id;
    @Indexed(unique = true)
    private String login;
    private String password;
    private String firstName;
    private String lastName;
    private String country;
    private String language;
    private Gender gender;
    private Integer age;
    @Indexed(unique = true, sparse = true)
    private String phone;
    @Indexed(unique = true, sparse = true)
    private String email;
    private String telegramChatId;
    private Set<String> roles;
    private boolean blocked;
    private LocalDateTime createdAt;
}
