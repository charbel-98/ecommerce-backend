package com.charbel.ecommerce.orders.dto;

import com.charbel.ecommerce.orders.entity.Order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

	private UUID id;
	private UUID userId;
	private String userEmail;
	private String userFirstName;
	private String userLastName;
	private Integer totalAmount;
	private OrderStatus status;
	private List<OrderItemResponse> orderItems;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
