package com.charbel.ecommerce.event.dto;

import com.charbel.ecommerce.event.entity.Event;
import com.charbel.ecommerce.product.dto.ProductResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class EventResponse {

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
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private List<DiscountResponse> discounts;
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Set<ProductResponse> products;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;

	public static EventResponse fromEntity(Event event) {
		return EventResponse.builder().id(event.getId()).name(event.getName()).description(event.getDescription())
				.imageUrl(event.getImageUrl()).startDate(event.getStartDate()).endDate(event.getEndDate())
				.status(event.getStatus()).isCurrentlyActive(event.isActive())
				.discounts(event.getDiscounts() != null
						? event.getDiscounts().stream().map(DiscountResponse::fromEntity).collect(Collectors.toList())
						: null)
				.products(event.getProducts() != null
						? event.getProducts().stream().map(ProductResponse::fromEntity).collect(Collectors.toSet())
						: null)
				.createdAt(event.getCreatedAt()).updatedAt(event.getUpdatedAt()).build();
	}

	public static EventResponse fromEntityBasic(Event event) {
		return EventResponse.builder().id(event.getId()).name(event.getName()).description(event.getDescription())
				.imageUrl(event.getImageUrl()).startDate(event.getStartDate()).endDate(event.getEndDate())
				.status(event.getStatus()).isCurrentlyActive(event.isActive()).createdAt(event.getCreatedAt())
				.updatedAt(event.getUpdatedAt()).build();
	}

	public static EventResponse fromEntityWithDiscounts(Event event) {
		return EventResponse.builder().id(event.getId()).name(event.getName()).description(event.getDescription())
				.imageUrl(event.getImageUrl()).startDate(event.getStartDate()).endDate(event.getEndDate())
				.status(event.getStatus()).isCurrentlyActive(event.isActive())
				.discounts(event.getDiscounts() != null
						? event.getDiscounts().stream().map(DiscountResponse::fromEntity).collect(Collectors.toList())
						: null)
				.createdAt(event.getCreatedAt()).updatedAt(event.getUpdatedAt()).build();
	}
}