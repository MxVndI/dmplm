package com.diplom.domain.model;

import com.diplom.persistance.entity.Gender;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
public class User {

    private String id;

    private String password;
    private String firstName;
    private String lastName;
    private String country;
    private String language;
    private Gender gender;
    private Integer age;

    /** Telegram chat ID — set when user links their Telegram account via /start */
    private String telegramChatId;

    private Set<String> roles;
    private LocalDateTime createdAt;
}
