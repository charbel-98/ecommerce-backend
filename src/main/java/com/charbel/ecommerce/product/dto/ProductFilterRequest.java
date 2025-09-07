package com.charbel.ecommerce.product.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterRequest {

    @DecimalMin(value = "0.0", message = "Minimum price must be zero or positive")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.0", message = "Maximum price must be zero or positive")
    private BigDecimal maxPrice;

    private List<String> colors;

    private List<String> sizes;

    private List<String> brandSlugs;
}