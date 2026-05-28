package com.diplom.persistance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Persisted record of a completed purchase order.
 */
@Data
@NoArgsConstructor
@Document(collection = "orders")
public class OrderEntity {

    @Id
    private String id;

    private String userId;
    private List<OrderItem> items;
    private BigDecimal totalPrice;
    private String status; // PENDING, COMPLETED, CANCELLED

    /** A/B test context at the time of purchase — key for conversion analysis. */
    private String testId;
    private String variant;

    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    public static class OrderItem {
        private String productId;
        private String productName;
        private BigDecimal price;
        private int quantity;
    }
}
