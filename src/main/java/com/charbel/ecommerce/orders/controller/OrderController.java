package com.charbel.ecommerce.orders.controller;

import com.charbel.ecommerce.orders.dto.OrderResponse;
import com.charbel.ecommerce.orders.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Orders", description = "Admin order management endpoints")
public class OrderController {

	private final OrderService orderService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
		summary = "Get all orders",
		description = "Returns all orders with details. Admin only.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	public ResponseEntity<List<OrderResponse>> getAllOrders() {
		log.info("Admin requesting all orders");
		List<OrderResponse> response = orderService.getAllOrders();
		return ResponseEntity.ok(response);
	}
}