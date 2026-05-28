package com.diplom.rest.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDto {

    @NotBlank(message = "Product name is required")
    @Size(max = 200)
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price format: up to 10 digits and 2 decimal places")
    private BigDecimal price;

    @Size(max = 2000, message = "Description is too long (max 2000 characters)")
    private String description;

    @NotNull(message = "Available quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer availableQuantity;
}
