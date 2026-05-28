package com.diplom.rest.dto;

import com.diplom.persistance.entity.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRegistrationDto {

    @NotBlank(message = "Login is required")
    @Size(min = 3, max = 50, message = "Login must be 3-50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Login may only contain letters, digits and underscores")
    private String login;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @Size(max = 100)
    private String country;

    @Size(max = 100)
    private String language;

    private Gender gender;

    @Min(value = 1, message = "Age must be positive")
    @Max(value = 150, message = "Age seems invalid")
    private Integer age;

    @Pattern(regexp = "^\\+?[0-9\\s\\-()]{7,20}$", message = "Invalid phone number format")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String email;

    // ── Optional demographic fields (forwarded to demographic-service) ──────

    /** LOW, MEDIUM, HIGH — optional */
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH)?$", message = "Income level must be LOW, MEDIUM or HIGH")
    private String incomeLevel;

    /** BASIC, SECONDARY, HIGHER, ACADEMIC — optional */
    @Pattern(regexp = "^(BASIC|SECONDARY|HIGHER|ACADEMIC)?$", message = "Education level must be BASIC, SECONDARY, HIGHER or ACADEMIC")
    private String educationLevel;

    /** STUDENT, EMPLOYED, SELF_EMPLOYED, UNEMPLOYED, RETIRED — optional */
    @Pattern(regexp = "^(STUDENT|EMPLOYED|SELF_EMPLOYED|UNEMPLOYED|RETIRED)?$", message = "Occupation must be a valid value")
    private String occupation;

    /** Comma-separated interests, e.g. "technology, sports, music" — optional */
    @Size(max = 500)
    private String interestsRaw;
}
