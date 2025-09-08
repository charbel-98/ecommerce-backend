package com.charbel.ecommerce.orders.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.charbel.ecommerce.orders.dto.AdminOrderResponseDTO;
import com.charbel.ecommerce.orders.dto.BillResponse;
import com.charbel.ecommerce.orders.dto.CreateOrderRequest;
import com.charbel.ecommerce.orders.dto.CreateOrderResponse;
import com.charbel.ecommerce.orders.dto.PaginatedAdminOrdersResponse;
import com.charbel.ecommerce.orders.dto.PaginatedOrdersResponse;
import com.charbel.ecommerce.orders.dto.UpdateOrderStatusRequest;
import com.charbel.ecommerce.orders.entity.Order.OrderStatus;
import com.charbel.ecommerce.orders.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

	private final OrderService orderService;

	@PostMapping("/orders")
	@PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
	@Operation(summary = "Create a new order", description = "Creates a new order for the authenticated customer", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
		log.info("Creating new order with {} items", request.getItems().size());
		CreateOrderResponse response = orderService.createOrder(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/orders/me")
	@PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
	@Operation(summary = "Get current user's orders", description = "Returns paginated orders for the authenticated customer, optionally filtered by status(es). Multiple statuses can be provided: ?status=PENDING,SHIPPED or ?status=PENDING&status=SHIPPED", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<PaginatedOrdersResponse> getUserOrders(
			@RequestParam(required = false, name = "status") String[] statusArray,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sort,
			@RequestParam(defaultValue = "desc") String direction) {
		log.info("Received status parameters: {}", statusArray != null ? Arrays.toString(statusArray) : "null");

		// Create pagination object
		Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

		if (statusArray != null && statusArray.length > 0) {
			List<OrderStatus> statuses = new ArrayList<>();
			for (String statusStr : statusArray) {
				// Handle comma-separated values in a single parameter
				if (statusStr.contains(",")) {
					for (String individualStatus : statusStr.split(",")) {
						try {
							statuses.add(OrderStatus.valueOf(individualStatus.trim()));
						} catch (IllegalArgumentException e) {
							log.warn("Invalid order status: {}", individualStatus.trim());
						}
					}
				} else {
					try {
						statuses.add(OrderStatus.valueOf(statusStr.trim()));
					} catch (IllegalArgumentException e) {
						log.warn("Invalid order status: {}", statusStr.trim());
					}
				}
			}

			if (!statuses.isEmpty()) {
				log.info("Fetching orders for current user with statuses: {}", statuses);
				PaginatedOrdersResponse response = orderService.getUserOrdersByStatusesPaginated(statuses, pageable);
				return ResponseEntity.ok(response);
			}
		}

		log.info("Fetching all orders for current user");
		PaginatedOrdersResponse response = orderService.getUserOrdersPaginated(pageable);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/orders/bill")
	@PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
	@Operation(summary = "Calculate order bill", description = "Calculates the bill for an order including discounts and delivery fees", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<BillResponse> calculateBill(@Valid @RequestBody CreateOrderRequest request) {
		log.info("Calculating bill for order with {} items", request.getItems().size());
		BillResponse response = orderService.calculateBill(request);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/admin/orders")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Get all orders", description = "Returns paginated orders with full user and address details, optionally filtered by status(es). Multiple statuses can be provided: ?status=PENDING,SHIPPED or ?status=PENDING&status=SHIPPED. Admin only.", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<PaginatedAdminOrdersResponse> getAllOrders(
			@RequestParam(required = false, name = "status") String[] statusArray,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sort,
			@RequestParam(defaultValue = "desc") String direction) {
		log.info("Admin received status parameters: {}", statusArray != null ? Arrays.toString(statusArray) : "null");

		// Create pagination object
		Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

		if (statusArray != null && statusArray.length > 0) {
			List<OrderStatus> statuses = new ArrayList<>();
			for (String statusStr : statusArray) {
				// Handle comma-separated values in a single parameter
				if (statusStr.contains(",")) {
					for (String individualStatus : statusStr.split(",")) {
						try {
							statuses.add(OrderStatus.valueOf(individualStatus.trim()));
						} catch (IllegalArgumentException e) {
							log.warn("Invalid order status: {}", individualStatus.trim());
						}
					}
				} else {
					try {
						statuses.add(OrderStatus.valueOf(statusStr.trim()));
					} catch (IllegalArgumentException e) {
						log.warn("Invalid order status: {}", statusStr.trim());
					}
				}
			}

			if (!statuses.isEmpty()) {
				log.info("Admin fetching orders with statuses: {}", statuses);
				PaginatedAdminOrdersResponse response = orderService.getAllOrdersForAdminByStatusesPaginated(statuses, pageable);
				return ResponseEntity.ok(response);
			}
		}

		log.info("Admin requesting all orders with full details and pagination");
		PaginatedAdminOrdersResponse response = orderService.getAllOrdersForAdminPaginated(pageable);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/admin/orders/{orderId}/status")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Update order status", description = "Updates the status of an order and returns full order details with user and address info. Admin only.", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<AdminOrderResponseDTO> updateOrderStatus(
			@PathVariable UUID orderId,
			@Valid @RequestBody UpdateOrderStatusRequest request) {
		log.info("=== UPDATE ORDER STATUS ENDPOINT CALLED ===");
		log.info("Order ID: {}", orderId);
		log.info("New Status: {}", request.getStatus());
		log.info("Current Authentication: {}", org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication());
		log.info("User Authorities: {}", org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities());
		
		AdminOrderResponseDTO response = orderService.updateOrderStatusForAdmin(orderId, request);
		return ResponseEntity.ok(response);
	}
}
