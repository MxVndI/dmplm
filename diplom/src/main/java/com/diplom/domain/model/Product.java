package com.diplom.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Product {

    private String id;
    private String name;
    private BigDecimal price;
    private String description;
    private String photoKey;
    private String photoUrl;
    private Integer availableQuantity;
    private LocalDateTime createdAt;
}
