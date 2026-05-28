package com.diplom.persistance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "products")
public class ProductEntity {

    @Id
    private String id;

    private String name;
    private BigDecimal price;
    private String description;

    /** S3 object key (used for deletion). */
    private String photoKey;

    /** Public URL to the product photo. */
    private String photoUrl;

    private Integer availableQuantity;
    private LocalDateTime createdAt;
}
