package com.diplom.rest.dto;

import com.diplom.constant.AppConstants;
import com.diplom.persistance.entity.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRegistrationDto {

    @NotBlank(message = AppConstants.LOGIN_REQUIRED)
    @Size(min = 3, max = 50, message = AppConstants.LOGIN_SIZE_INVALID)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = AppConstants.LOGIN_FORMAT_INVALID)
    private String login;
    @NotBlank(message = AppConstants.PASSWORD_REQUIRED)
    @Size(min = 8, message = AppConstants.PASSWORD_SIZE_INVALID)
    private String password;
    @NotBlank(message = AppConstants.FIRST_NAME_REQUIRED)
    @Size(max = 100)
    private String firstName;
    @NotBlank(message = AppConstants.LAST_NAME_REQUIRED)
    @Size(max = 100)
    private String lastName;
    @Size(max = 100)
    private String country;
    @Size(max = 100)
    private String language;
    private Gender gender;
    @Min(value = 1, message = AppConstants.AGE_POSITIVE)
    @Max(value = 150, message = AppConstants.AGE_INVALID)
    private Integer age;
    @Pattern(regexp = "^\\+?[0-9\\s\\-()]{7,20}$", message = AppConstants.PHONE_INVALID)
    private String phone;
    @NotBlank(message = AppConstants.EMAIL_REQUIRED)
    @Email(message = AppConstants.EMAIL_INVALID)
    @Size(max = 255)
    private String email;
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH)?$", message = AppConstants.INCOME_LEVEL_INVALID)
    private String incomeLevel;
    @Pattern(regexp = "^(BASIC|SECONDARY|HIGHER|ACADEMIC)?$", message = AppConstants.EDUCATION_LEVEL_INVALID)
    private String educationLevel;
    @Pattern(regexp = "^(STUDENT|EMPLOYED|SELF_EMPLOYED|UNEMPLOYED|RETIRED)?$", message = AppConstants.OCCUPATION_INVALID)
    private String occupation;
    @Size(max = 500)
    private String interestsRaw;
}
