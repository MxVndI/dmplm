package com.diplom.rest.dto;

import com.diplom.constant.AppConstants;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDto {

    @NotBlank(message = AppConstants.PRODUCT_NAME_REQUIRED)
    @Size(max = 200)
    private String name;

    @NotNull(message = AppConstants.PRICE_REQUIRED)
    @DecimalMin(value = "0.01", message = AppConstants.PRICE_POSITIVE)
    @Digits(integer = 10, fraction = 2, message = AppConstants.PRICE_FORMAT_INVALID)
    private BigDecimal price;

    @Size(max = 2000, message = AppConstants.DESCRIPTION_TOO_LONG)
    private String description;

    @NotNull(message = AppConstants.QUANTITY_REQUIRED)
    @Min(value = 0, message = AppConstants.QUANTITY_NEGATIVE)
    private Integer availableQuantity;
}
