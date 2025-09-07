package com.charbel.ecommerce.orders.dto;

import com.charbel.ecommerce.orders.entity.Order;
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
public class CreateOrderResponse {

	private UUID orderId;
	private UUID addressId;
	private Integer originalAmount;
	private Integer discountAmount;
	private Integer totalAmount;
	private Order.OrderStatus status;
	private List<OrderItemResponse> orderItems;
	private LocalDateTime createdAt;
}