package com.charbel.ecommerce.event.dto;

import com.charbel.ecommerce.event.entity.Discount;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiscountRequest {

	@NotNull(message = "Discount type is required")
	private Discount.DiscountType type;

	@NotNull(message = "Discount value is required")
	@DecimalMin(value = "0.01", message = "Discount value must be at least 0.01")
	private BigDecimal value; // percentage (1.00-100.00) or fixed amount in dollars

	@DecimalMin(value = "0.0", message = "Minimum purchase amount must be non-negative")
	private BigDecimal minPurchaseAmount; // minimum purchase required in dollars

	@DecimalMin(value = "0.0", message = "Maximum discount amount must be non-negative")
	private BigDecimal maxDiscountAmount; // max discount for percentage type in dollars

	public Discount toEntity() {
		return Discount.builder().type(type).value(value).minPurchaseAmount(minPurchaseAmount)
				.maxDiscountAmount(maxDiscountAmount).build();
	}
}
