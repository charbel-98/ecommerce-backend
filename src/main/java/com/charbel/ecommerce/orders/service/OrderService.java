package com.charbel.ecommerce.orders.service;

import com.charbel.ecommerce.orders.dto.OrderItemResponse;
import com.charbel.ecommerce.orders.dto.OrderResponse;
import com.charbel.ecommerce.orders.entity.Order;
import com.charbel.ecommerce.orders.entity.OrderItem;
import com.charbel.ecommerce.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

	private final OrderRepository orderRepository;

	public List<OrderResponse> getAllOrders() {
		log.info("Fetching all orders for admin");
		List<Order> orders = orderRepository.findAllOrdersWithDetails();
		
		return orders.stream()
				.map(this::mapToOrderResponse)
				.collect(Collectors.toList());
	}

	private OrderResponse mapToOrderResponse(Order order) {
		List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
				.map(this::mapToOrderItemResponse)
				.collect(Collectors.toList());

		return OrderResponse.builder()
				.id(order.getId())
				.userId(order.getUser().getId())
				.userEmail(order.getUser().getEmail())
				.userFirstName(order.getUser().getFirstName())
				.userLastName(order.getUser().getLastName())
				.totalAmount(order.getTotalAmount())
				.status(order.getStatus())
				.orderItems(orderItemResponses)
				.createdAt(order.getCreatedAt())
				.updatedAt(order.getUpdatedAt())
				.build();
	}

	private OrderItemResponse mapToOrderItemResponse(OrderItem orderItem) {
		return OrderItemResponse.builder()
				.id(orderItem.getId())
				.variantId(orderItem.getVariant().getId())
				.sku(orderItem.getVariant().getSku())
				.attributes(orderItem.getVariant().getAttributes())
				.productName(orderItem.getVariant().getProduct().getName())
				.quantity(orderItem.getQuantity())
				.unitPrice(orderItem.getUnitPrice())
				.totalPrice(orderItem.getQuantity() * orderItem.getUnitPrice())
				.build();
	}
}