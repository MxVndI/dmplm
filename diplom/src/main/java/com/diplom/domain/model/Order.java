package com.diplom.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Persisted record of a completed purchase order.
 */
@Data
@NoArgsConstructor
public class Order {

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
