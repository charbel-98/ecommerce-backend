package com.charbel.ecommerce.product.dto;

import com.charbel.ecommerce.event.entity.Discount;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountInfo {
    
    private UUID eventId;
    private String eventName;
    private UUID discountId;
    private Discount.DiscountType type;
    private BigDecimal value;
    private BigDecimal minPurchaseAmount;
    private BigDecimal maxDiscountAmount;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventStartDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventEndDate;
    
    public BigDecimal calculateDiscountAmount(BigDecimal totalAmount) {
        if (minPurchaseAmount != null && totalAmount.compareTo(minPurchaseAmount) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discountAmount;
        if (type == Discount.DiscountType.PERCENTAGE) {
            // For percentage, value represents the percentage (e.g., 25.00 for 25%)
            discountAmount = totalAmount.multiply(value).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            if (maxDiscountAmount != null && discountAmount.compareTo(maxDiscountAmount) > 0) {
                discountAmount = maxDiscountAmount;
            }
        } else {
            // For fixed amount, value is the discount amount in dollars
            discountAmount = value;
        }

        return discountAmount.min(totalAmount);
    }
}