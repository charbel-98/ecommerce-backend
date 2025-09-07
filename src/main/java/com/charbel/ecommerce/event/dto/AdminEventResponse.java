package com.charbel.ecommerce.event.dto;

import com.charbel.ecommerce.event.entity.Event;
import com.charbel.ecommerce.product.dto.ProductResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class AdminEventResponse {

	private UUID id;
	private String name;
	private String description;
	private String imageUrl;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime startDate;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime endDate;

	private Event.EventStatus status;
	private boolean isCurrentlyActive;

	// Full discount details for admin
	private List<DiscountResponse> discounts;

	// Full product details for admin with all related data
	private List<ProductResponse> products;

	// Additional admin-specific data
	private int discountCount;
	private int productCount;
	private String totalDiscountSummary;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;

	public static AdminEventResponse fromEntity(Event event) {
		List<DiscountResponse> discounts = event.getDiscounts() != null
				? event.getDiscounts().stream().map(DiscountResponse::fromEntity).collect(Collectors.toList())
				: List.of();

		List<ProductResponse> products = event.getProducts() != null
				? event.getProducts().stream().map(ProductResponse::fromEntity).collect(Collectors.toList())
				: List.of();

		return fromEntity(event, discounts, products);
	}

	public static AdminEventResponse fromEntity(Event event, List<DiscountResponse> discounts, List<ProductResponse> products) {
		// Generate discount summary for admin overview
		String discountSummary = generateDiscountSummary(discounts);

		return AdminEventResponse.builder().id(event.getId()).name(event.getName()).description(event.getDescription())
				.imageUrl(event.getImageUrl()).startDate(event.getStartDate()).endDate(event.getEndDate())
				.status(event.getStatus()).isCurrentlyActive(event.isActive()).discounts(discounts).products(products)
				.discountCount(discounts.size()).productCount(products.size()).totalDiscountSummary(discountSummary)
				.createdAt(event.getCreatedAt()).updatedAt(event.getUpdatedAt()).build();
	}

	private static String generateDiscountSummary(List<DiscountResponse> discounts) {
		if (discounts.isEmpty()) {
			return "No discounts";
		}

		return discounts.stream().map(discount -> {
			if (discount.getType().name().equals("PERCENTAGE")) {
				return discount.getValue() + "%";
			} else {
				return "$" + String.format("%.2f", discount.getValue().divide(new BigDecimal("100")).doubleValue());
			}
		}).collect(Collectors.joining(", "));
	}
}
