package com.charbel.ecommerce.orders.service;

import com.charbel.ecommerce.address.entity.Address;
import com.charbel.ecommerce.address.repository.AddressRepository;
import com.charbel.ecommerce.event.entity.Discount;
import com.charbel.ecommerce.event.entity.Event;
import com.charbel.ecommerce.event.repository.EventRepository;
import com.charbel.ecommerce.orders.dto.CreateOrderRequest;
import com.charbel.ecommerce.orders.dto.CreateOrderResponse;
import com.charbel.ecommerce.orders.dto.OrderItemResponse;
import com.charbel.ecommerce.orders.dto.OrderResponse;
import com.charbel.ecommerce.orders.entity.Order;
import com.charbel.ecommerce.orders.entity.OrderItem;
import com.charbel.ecommerce.orders.repository.OrderRepository;
import com.charbel.ecommerce.product.entity.Product;
import com.charbel.ecommerce.product.entity.ProductVariant;
import com.charbel.ecommerce.product.repository.ProductVariantRepository;
import com.charbel.ecommerce.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

	private final OrderRepository orderRepository;
	private final ProductVariantRepository productVariantRepository;
	private final AddressRepository addressRepository;
	private final EventRepository eventRepository;

	public List<OrderResponse> getAllOrders() {
		log.info("Fetching all orders for admin");
		List<Order> orders = orderRepository.findAllOrdersWithDetails();

		return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
	}

	@Transactional
	public CreateOrderResponse createOrder(CreateOrderRequest request) {
		User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		// Validate address ownership
		Address address = validateAddressOwnership(request.getAddressId(), currentUser.getId());
		
		// Fetch and validate variants
		List<UUID> variantIds = request.getItems().stream()
			.map(CreateOrderRequest.OrderItemRequest::getVariantId)
			.collect(Collectors.toList());
		
		List<ProductVariant> variants = productVariantRepository.findByIdInWithProduct(variantIds);
		
		if (variants.size() != variantIds.size()) {
			throw new EntityNotFoundException("One or more product variants not found");
		}
		
		// Validate stock and create order items
		List<OrderItem> orderItems = new ArrayList<>();
		Map<UUID, Integer> variantQuantityMap = request.getItems().stream()
			.collect(Collectors.toMap(
				CreateOrderRequest.OrderItemRequest::getVariantId, 
				CreateOrderRequest.OrderItemRequest::getQuantity
			));
		
		// Calculate original total
		Integer originalTotalCents = 0;
		for (ProductVariant variant : variants) {
			Integer requestedQuantity = variantQuantityMap.get(variant.getId());
			
			if (variant.getStock() < requestedQuantity) {
				throw new IllegalArgumentException(
					String.format("Insufficient stock for product %s. Available: %d, Requested: %d", 
						variant.getProduct().getName(), variant.getStock(), requestedQuantity)
				);
			}
			
			// Convert price to cents for storage
			Integer unitPriceCents = variant.getPrice().multiply(BigDecimal.valueOf(100)).intValue();
			originalTotalCents += unitPriceCents * requestedQuantity;
		}
		
		// Calculate discounts
		Map<UUID, Discount> productDiscountMap = findApplicableDiscounts(variants);
		Integer discountAmountCents = calculateTotalDiscount(variants, variantQuantityMap, productDiscountMap, originalTotalCents);
		Integer finalTotalCents = originalTotalCents - discountAmountCents;
		
		// Create order
		Order order = Order.builder()
			.user(currentUser)
			.address(address)
			.originalAmount(originalTotalCents)
			.discountAmount(discountAmountCents)
			.totalAmount(finalTotalCents)
			.status(Order.OrderStatus.PENDING)
			.build();
		
		Order savedOrder = orderRepository.save(order);
		
		// Create order items and update stock
		for (ProductVariant variant : variants) {
			Integer requestedQuantity = variantQuantityMap.get(variant.getId());
			Integer unitPriceCents = variant.getPrice().multiply(BigDecimal.valueOf(100)).intValue();
			
			// Apply discount to unit price if applicable
			if (productDiscountMap.containsKey(variant.getProduct().getId())) {
				Discount discount = productDiscountMap.get(variant.getProduct().getId());
				BigDecimal itemTotal = BigDecimal.valueOf(unitPriceCents * requestedQuantity);
				BigDecimal itemDiscountAmount = discount.calculateDiscountAmount(itemTotal.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
				Integer itemDiscountCents = itemDiscountAmount.multiply(BigDecimal.valueOf(100)).intValue();
				unitPriceCents = (unitPriceCents * requestedQuantity - itemDiscountCents) / requestedQuantity;
			}
			
			OrderItem orderItem = OrderItem.builder()
				.order(savedOrder)
				.variant(variant)
				.quantity(requestedQuantity)
				.unitPrice(unitPriceCents)
				.build();
			
			orderItems.add(orderItem);
			
			// Update stock
			variant.setStock(variant.getStock() - requestedQuantity);
			productVariantRepository.save(variant);
		}
		
		savedOrder.setOrderItems(orderItems);
		
		log.info("Created order {} for user {} with total amount {}", 
			savedOrder.getId(), currentUser.getId(), finalTotalCents);
		
		return mapToCreateOrderResponse(savedOrder);
	}

	public List<OrderResponse> getUserOrders() {
		User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		List<Order> orders = orderRepository.findByUserIdWithDetails(currentUser.getId());
		return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
	}

	private Address validateAddressOwnership(UUID addressId, UUID userId) {
		Address address = addressRepository.findById(addressId)
			.orElseThrow(() -> new EntityNotFoundException("Address not found"));
		
		if (!address.getUser().getId().equals(userId)) {
			throw new IllegalArgumentException("Address does not belong to the current user");
		}
		
		return address;
	}

	private Map<UUID, Discount> findApplicableDiscounts(List<ProductVariant> variants) {
		List<UUID> productIds = variants.stream()
			.map(variant -> variant.getProduct().getId())
			.collect(Collectors.toList());
		
		List<Event> activeEventsWithDiscounts = eventRepository
			.findActiveEventsWithDiscountsForProducts(productIds, LocalDateTime.now());
		
		Map<UUID, Discount> productDiscountMap = new HashMap<>();
		
		for (Event event : activeEventsWithDiscounts) {
			for (Product product : event.getProducts()) {
				if (productIds.contains(product.getId()) && !event.getDiscounts().isEmpty()) {
					// Use the first discount from the event (assuming one discount per event)
					productDiscountMap.put(product.getId(), event.getDiscounts().get(0));
				}
			}
		}
		
		return productDiscountMap;
	}

	private Integer calculateTotalDiscount(List<ProductVariant> variants, 
										   Map<UUID, Integer> variantQuantityMap,
										   Map<UUID, Discount> productDiscountMap,
										   Integer originalTotalCents) {
		Integer totalDiscountCents = 0;
		
		for (ProductVariant variant : variants) {
			if (productDiscountMap.containsKey(variant.getProduct().getId())) {
				Integer requestedQuantity = variantQuantityMap.get(variant.getId());
				Integer unitPriceCents = variant.getPrice().multiply(BigDecimal.valueOf(100)).intValue();
				BigDecimal itemTotal = BigDecimal.valueOf(unitPriceCents * requestedQuantity).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
				
				Discount discount = productDiscountMap.get(variant.getProduct().getId());
				BigDecimal itemDiscountAmount = discount.calculateDiscountAmount(itemTotal);
				totalDiscountCents += itemDiscountAmount.multiply(BigDecimal.valueOf(100)).intValue();
			}
		}
		
		return totalDiscountCents;
	}

	private CreateOrderResponse mapToCreateOrderResponse(Order order) {
		List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
			.map(this::mapToOrderItemResponse)
			.collect(Collectors.toList());

		return CreateOrderResponse.builder()
			.orderId(order.getId())
			.addressId(order.getAddress().getId())
			.originalAmount(order.getOriginalAmount())
			.discountAmount(order.getDiscountAmount())
			.totalAmount(order.getTotalAmount())
			.status(order.getStatus())
			.orderItems(orderItemResponses)
			.createdAt(order.getCreatedAt())
			.build();
	}

	private OrderResponse mapToOrderResponse(Order order) {
		List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream().map(this::mapToOrderItemResponse)
				.collect(Collectors.toList());

		return OrderResponse.builder().id(order.getId()).userId(order.getUser().getId())
				.userEmail(order.getUser().getEmail()).userFirstName(order.getUser().getFirstName())
				.userLastName(order.getUser().getLastName()).totalAmount(order.getTotalAmount())
				.status(order.getStatus()).orderItems(orderItemResponses).createdAt(order.getCreatedAt())
				.updatedAt(order.getUpdatedAt()).build();
	}

	private OrderItemResponse mapToOrderItemResponse(OrderItem orderItem) {
		return OrderItemResponse.builder().id(orderItem.getId()).variantId(orderItem.getVariant().getId())
				.sku(orderItem.getVariant().getSku()).attributes(orderItem.getVariant().getAttributes())
				.productName(orderItem.getVariant().getProduct().getName()).quantity(orderItem.getQuantity())
				.unitPrice(orderItem.getUnitPrice()).totalPrice(orderItem.getQuantity() * orderItem.getUnitPrice())
				.build();
	}
}
