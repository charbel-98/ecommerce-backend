package com.charbel.ecommerce.event.dto;

import com.charbel.ecommerce.event.entity.Discount;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DiscountRequest {

	@NotNull(message = "Discount type is required")
	private Discount.DiscountType type;

	@NotNull(message = "Discount value is required")
	@Min(value = 1, message = "Discount value must be at least 1")
	private Integer value; // percentage (1-100) or fixed amount in cents

	@Min(value = 0, message = "Minimum purchase amount must be non-negative")
	private Integer minPurchaseAmount; // minimum purchase required in cents

	@Min(value = 0, message = "Maximum discount amount must be non-negative")
	private Integer maxDiscountAmount; // max discount for percentage type in cents

	public Discount toEntity() {
		return Discount.builder().type(type).value(value).minPurchaseAmount(minPurchaseAmount)
				.maxDiscountAmount(maxDiscountAmount).build();
	}
}