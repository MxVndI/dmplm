package com.diplom.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileEvent {

    private String id;
    private Integer age;
    private String country;
    private String language;
    private String gender;
}
