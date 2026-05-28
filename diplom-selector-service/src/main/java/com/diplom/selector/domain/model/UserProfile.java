package com.diplom.selector.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private String id;
    private Integer age;
    private String country;
    private String language;
    private String gender;
}
