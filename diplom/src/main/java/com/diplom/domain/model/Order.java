package com.diplom.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class Order {

    private String id;

    private String userId;
    private List<OrderItem> items;
    private BigDecimal totalPrice;
    private String status;

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
