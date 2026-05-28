package com.diplom.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ABTest {

    private String id;

    private String name;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;

    /** Optional deadline — test is auto-deactivated after this point. */
    private LocalDateTime expiresAt;
}
