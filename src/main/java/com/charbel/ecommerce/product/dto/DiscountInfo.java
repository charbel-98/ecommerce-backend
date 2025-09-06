package com.charbel.ecommerce.product.dto;

import com.charbel.ecommerce.event.entity.Discount;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Integer value;
    private Integer minPurchaseAmount;
    private Integer maxDiscountAmount;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventStartDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventEndDate;
    
    public int calculateDiscountAmount(int totalAmount) {
        if (minPurchaseAmount != null && totalAmount < minPurchaseAmount) {
            return 0;
        }

        int discountAmount;
        if (type == Discount.DiscountType.PERCENTAGE) {
            discountAmount = (totalAmount * value) / 100;
            if (maxDiscountAmount != null && discountAmount > maxDiscountAmount) {
                discountAmount = maxDiscountAmount;
            }
        } else {
            discountAmount = value;
        }

        return Math.min(discountAmount, totalAmount);
    }
}