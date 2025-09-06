package com.charbel.ecommerce.event.dto;

import com.charbel.ecommerce.event.entity.Discount;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DiscountResponse {

	private UUID id;
	private Discount.DiscountType type;
	private Integer value;
	private Integer minPurchaseAmount;
	private Integer maxDiscountAmount;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;

	public static DiscountResponse fromEntity(Discount discount) {
		return DiscountResponse.builder().id(discount.getId()).type(discount.getType()).value(discount.getValue())
				.minPurchaseAmount(discount.getMinPurchaseAmount())
				.maxDiscountAmount(discount.getMaxDiscountAmount()).createdAt(discount.getCreatedAt())
				.updatedAt(discount.getUpdatedAt()).build();
	}
}