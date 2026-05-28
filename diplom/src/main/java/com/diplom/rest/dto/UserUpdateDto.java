package com.diplom.rest.dto;

import com.diplom.persistance.entity.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserUpdateDto {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "Country is required")
    @Size(max = 100)
    private String country;

    @NotBlank(message = "Language is required")
    @Size(max = 100)
    private String language;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Age is required")
    @Min(value = 1, message = "Age must be positive")
    @Max(value = 150, message = "Age seems invalid")
    private Integer age;

    @Pattern(regexp = "^\\+?[0-9\\s\\-()]{7,20}$", message = "Invalid phone number format")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String email;

    @Pattern(regexp = "^(-?[0-9]{5,20})?$", message = "Telegram chat ID must be numeric")
    private String telegramChatId;

    // ── Optional demographic fields ───────────────────────────────────────────
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH)?$", message = "Income level must be LOW, MEDIUM or HIGH")
    private String incomeLevel;

    @Pattern(regexp = "^(BASIC|SECONDARY|HIGHER|ACADEMIC)?$", message = "Education must be BASIC, SECONDARY, HIGHER or ACADEMIC")
    private String educationLevel;

    @Pattern(regexp = "^(STUDENT|EMPLOYED|SELF_EMPLOYED|UNEMPLOYED|RETIRED)?$", message = "Occupation must be a valid value")
    private String occupation;

    @Size(max = 500)
    private String interestsRaw;
}
