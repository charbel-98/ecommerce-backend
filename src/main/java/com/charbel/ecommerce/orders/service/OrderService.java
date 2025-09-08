package com.charbel.ecommerce.orders.service;

import com.charbel.ecommerce.address.entity.Address;
import com.charbel.ecommerce.address.repository.AddressRepository;
import com.charbel.ecommerce.event.entity.Discount;
import com.charbel.ecommerce.event.entity.Event;
import com.charbel.ecommerce.event.repository.EventRepository;
import com.charbel.ecommerce.orders.dto.AdminOrderResponseDTO;
import com.charbel.ecommerce.orders.dto.BillResponse;
import com.charbel.ecommerce.orders.dto.CreateOrderRequest;
import com.charbel.ecommerce.orders.dto.CreateOrderResponse;
import com.charbel.ecommerce.orders.dto.OrderItemResponse;
import com.charbel.ecommerce.orders.dto.OrderResponse;
import com.charbel.ecommerce.orders.dto.PaginatedAdminOrdersResponse;
import com.charbel.ecommerce.orders.dto.PaginatedOrdersResponse;
import com.charbel.ecommerce.orders.dto.UpdateOrderStatusRequest;
import com.charbel.ecommerce.product.dto.ProductImageResponse;
import com.charbel.ecommerce.product.entity.ProductImage;
import com.charbel.ecommerce.orders.entity.Order;
import com.charbel.ecommerce.orders.entity.OrderItem;
import com.charbel.ecommerce.orders.repository.OrderRepository;
import com.charbel.ecommerce.product.entity.Product;
import com.charbel.ecommerce.product.entity.ProductVariant;
import com.charbel.ecommerce.product.repository.ProductImageRepository;
import com.charbel.ecommerce.product.repository.ProductVariantRepository;
import com.charbel.ecommerce.user.entity.User;
import com.charbel.ecommerce.service.SecurityService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

	private final OrderRepository orderRepository;
	private final ProductVariantRepository productVariantRepository;
	private final ProductImageRepository productImageRepository;
	private final AddressRepository addressRepository;
	private final EventRepository eventRepository;
	private final SecurityService securityService;

	public List<OrderResponse> getAllOrders() {
		log.info("Fetching all orders for admin");
		List<Order> orders = orderRepository.findAllOrdersWithDetails();

		return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
	}

	@Transactional
	public CreateOrderResponse createOrder(CreateOrderRequest request) {
		User currentUser = securityService.getCurrentUser();
		
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
		BigDecimal originalTotal = BigDecimal.ZERO;
		for (ProductVariant variant : variants) {
			Integer requestedQuantity = variantQuantityMap.get(variant.getId());
			
			if (variant.getStock() < requestedQuantity) {
				throw new IllegalArgumentException(
					String.format("Insufficient stock for product %s. Available: %d, Requested: %d", 
						variant.getProduct().getName(), variant.getStock(), requestedQuantity)
				);
			}
			
			BigDecimal itemTotal = variant.getPrice().multiply(BigDecimal.valueOf(requestedQuantity));
			originalTotal = originalTotal.add(itemTotal);
		}
		
		// Calculate discounts
		Map<UUID, Discount> productDiscountMap = findApplicableDiscounts(variants);
		BigDecimal discountAmount = calculateOrderDiscounts(variants, variantQuantityMap, productDiscountMap);
		
		// Add delivery fee
		BigDecimal deliveryFee = new BigDecimal("5.00");
		BigDecimal finalTotal = originalTotal.subtract(discountAmount).add(deliveryFee);
		
		// Create order
		Order order = Order.builder()
			.orderNumber(generateOrderNumber())
			.user(currentUser)
			.address(address)
			.originalAmount(originalTotal)
			.discountAmount(discountAmount)
			.deliveryFee(deliveryFee)
			.totalAmount(finalTotal)
			.status(Order.OrderStatus.PENDING)
			.build();
		
		Order savedOrder = orderRepository.save(order);
		
		// Create order items and update stock
		for (ProductVariant variant : variants) {
			Integer requestedQuantity = variantQuantityMap.get(variant.getId());
			BigDecimal unitPrice = variant.getPrice();
			
			// Apply discount to unit price if applicable
			if (productDiscountMap.containsKey(variant.getProduct().getId())) {
				Discount discount = productDiscountMap.get(variant.getProduct().getId());
				BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(requestedQuantity));
				BigDecimal itemDiscountAmount = discount.calculateDiscountAmount(itemTotal);
				BigDecimal discountedTotal = itemTotal.subtract(itemDiscountAmount);
				unitPrice = discountedTotal.divide(BigDecimal.valueOf(requestedQuantity), 2, RoundingMode.HALF_UP);
			}
			
			OrderItem orderItem = OrderItem.builder()
				.order(savedOrder)
				.variant(variant)
				.quantity(requestedQuantity)
				.unitPrice(unitPrice)
				.build();
			
			orderItems.add(orderItem);
			
			// Update stock
			variant.setStock(variant.getStock() - requestedQuantity);
			productVariantRepository.save(variant);
		}
		
		savedOrder.setOrderItems(orderItems);
		
		log.info("Created order {} for user {} with total amount {}", 
			savedOrder.getId(), currentUser.getId(), finalTotal);
		
		return mapToCreateOrderResponse(savedOrder);
	}

	private String generateOrderNumber() {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String orderNumber;
		int attempts = 0;
		int maxAttempts = 10;
		
		do {
			String randomSuffix = String.format("%06d", (int) (Math.random() * 1000000));
			orderNumber = "ORD" + timestamp + randomSuffix;
			attempts++;
		} while (orderRepository.existsByOrderNumber(orderNumber) && attempts < maxAttempts);
		
		if (attempts >= maxAttempts) {
			// Fallback to timestamp + UUID suffix if we can't find a unique number
			orderNumber = "ORD" + timestamp + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		}
		
		return orderNumber;
	}

	public List<OrderResponse> getUserOrders() {
		User currentUser = securityService.getCurrentUser();
		List<Order> orders = orderRepository.findByUserIdWithDetails(currentUser.getId());
		return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
	}

	public List<OrderResponse> getUserOrdersByStatus(Order.OrderStatus status) {
		User currentUser = securityService.getCurrentUser();
		List<Order> orders = orderRepository.findByUserIdAndStatusWithDetails(currentUser.getId(), status);
		return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
	}

	public List<OrderResponse> getUserOrdersByStatuses(List<Order.OrderStatus> statuses) {
		User currentUser = securityService.getCurrentUser();
		List<Order> orders = orderRepository.findByUserIdAndStatusInWithDetails(currentUser.getId(), statuses);
		return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
	}

	// Paginated methods
	public PaginatedOrdersResponse getAllOrdersPaginated(Pageable pageable) {
		log.info("Fetching all orders for admin with pagination");
		Page<Order> orderPage = orderRepository.findAllOrdersWithDetailsPaginated(pageable);
		return mapToPaginatedOrdersResponse(orderPage);
	}

	public PaginatedAdminOrdersResponse getAllOrdersForAdminPaginated(Pageable pageable) {
		log.info("Fetching all orders for admin with full details and pagination");
		Page<Order> orderPage = orderRepository.findAllOrdersWithDetailsPaginated(pageable);
		return mapToPaginatedAdminOrdersResponse(orderPage);
	}

	public PaginatedAdminOrdersResponse getAllOrdersForAdminByStatusesPaginated(List<Order.OrderStatus> statuses, Pageable pageable) {
		log.info("Fetching all orders for admin with full details, filtered by statuses: {} and pagination", statuses);
		Page<Order> orderPage = orderRepository.findAllOrdersWithDetailsByStatusesPaginated(statuses, pageable);
		return mapToPaginatedAdminOrdersResponse(orderPage);
	}

	public PaginatedOrdersResponse getUserOrdersPaginated(Pageable pageable) {
		User currentUser = securityService.getCurrentUser();
		Page<Order> orderPage = orderRepository.findByUserIdWithDetailsPaginated(currentUser.getId(), pageable);
		return mapToPaginatedOrdersResponse(orderPage);
	}

	public PaginatedOrdersResponse getUserOrdersByStatusesPaginated(List<Order.OrderStatus> statuses, Pageable pageable) {
		User currentUser = securityService.getCurrentUser();
		Page<Order> orderPage = orderRepository.findByUserIdAndStatusInWithDetailsPaginated(currentUser.getId(), statuses, pageable);
		return mapToPaginatedOrdersResponse(orderPage);
	}

	public BillResponse calculateBill(CreateOrderRequest request) {
		// Fetch and validate variants
		List<UUID> variantIds = request.getItems().stream()
			.map(CreateOrderRequest.OrderItemRequest::getVariantId)
			.collect(Collectors.toList());
		
		List<ProductVariant> variants = productVariantRepository.findByIdInWithProduct(variantIds);
		
		if (variants.size() != variantIds.size()) {
			throw new EntityNotFoundException("One or more product variants not found");
		}
		
		Map<UUID, Integer> variantQuantityMap = request.getItems().stream()
			.collect(Collectors.toMap(
				CreateOrderRequest.OrderItemRequest::getVariantId, 
				CreateOrderRequest.OrderItemRequest::getQuantity
			));
		
		// Validate stock availability
		for (ProductVariant variant : variants) {
			Integer requestedQuantity = variantQuantityMap.get(variant.getId());
			if (variant.getStock() < requestedQuantity) {
				throw new IllegalArgumentException(
					String.format("Insufficient stock for product %s. Available: %d, Requested: %d", 
						variant.getProduct().getName(), variant.getStock(), requestedQuantity)
				);
			}
		}
		
		// Calculate subtotal
		BigDecimal subtotal = BigDecimal.ZERO;
		List<BillResponse.BillItemResponse> billItems = new ArrayList<>();
		
		for (ProductVariant variant : variants) {
			Integer requestedQuantity = variantQuantityMap.get(variant.getId());
			BigDecimal unitPrice = variant.getPrice();
			BigDecimal totalItemPrice = unitPrice.multiply(BigDecimal.valueOf(requestedQuantity));
			
			subtotal = subtotal.add(totalItemPrice);
			
			BillResponse.BillItemResponse billItem = BillResponse.BillItemResponse.builder()
				.productName(variant.getProduct().getName())
				.sku(variant.getSku())
				.quantity(requestedQuantity)
				.unitPrice(unitPrice)
				.originalUnitPrice(unitPrice)
				.totalPrice(totalItemPrice)
				.discountAmount(BigDecimal.ZERO)
				.build();
			
			billItems.add(billItem);
		}
		
		// Calculate discounts
		Map<UUID, Discount> productDiscountMap = findApplicableDiscounts(variants);
		BigDecimal totalDiscountAmount = calculateBillDiscounts(variants, variantQuantityMap, productDiscountMap);
		
		// Apply discounts to bill items
		for (BillResponse.BillItemResponse billItem : billItems) {
			ProductVariant variant = variants.stream()
				.filter(v -> v.getSku().equals(billItem.getSku()))
				.findFirst()
				.orElse(null);
			
			if (variant != null && productDiscountMap.containsKey(variant.getProduct().getId())) {
				Integer requestedQuantity = variantQuantityMap.get(variant.getId());
				BigDecimal originalTotal = billItem.getOriginalUnitPrice().multiply(BigDecimal.valueOf(requestedQuantity));
				
				Discount discount = productDiscountMap.get(variant.getProduct().getId());
				BigDecimal itemDiscountAmount = discount.calculateDiscountAmount(originalTotal);
				
				BigDecimal discountedTotal = originalTotal.subtract(itemDiscountAmount);
				BigDecimal discountedUnitPrice = discountedTotal.divide(BigDecimal.valueOf(requestedQuantity), 2, RoundingMode.HALF_UP);
				
				billItem.setUnitPrice(discountedUnitPrice);
				billItem.setTotalPrice(discountedTotal);
				billItem.setDiscountAmount(itemDiscountAmount);
			}
		}
		
		// Fixed delivery fee of $5.00
		BigDecimal deliveryFee = new BigDecimal("5.00");
		
		// Calculate final total
		BigDecimal finalTotal = subtotal.subtract(totalDiscountAmount).add(deliveryFee);
		
		return BillResponse.builder()
			.subtotal(subtotal)
			.discountAmount(totalDiscountAmount)
			.deliveryFee(deliveryFee)
			.totalAmount(finalTotal)
			.items(billItems)
			.build();
	}
	
	@Transactional
	public OrderResponse updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request) {
		User currentUser = securityService.getCurrentUser();
		
		Order order = orderRepository.findByIdAndNotDeleted(orderId)
			.orElseThrow(() -> new EntityNotFoundException("Order not found"));
		
		order.setStatus(request.getStatus());
		Order savedOrder = orderRepository.save(order);
		
		log.info("Updated order {} status to {} by user {}", 
			orderId, request.getStatus(), currentUser.getId());
		
		return mapToOrderResponse(savedOrder);
	}

	@Transactional
	public AdminOrderResponseDTO updateOrderStatusForAdmin(UUID orderId, UpdateOrderStatusRequest request) {
		User currentUser = securityService.getCurrentUser();
		
		Order order = orderRepository.findByIdWithDetails(orderId)
			.orElseThrow(() -> new EntityNotFoundException("Order not found"));
		
		order.setStatus(request.getStatus());
		Order savedOrder = orderRepository.save(order);
		
		log.info("Updated order {} status to {} by admin user {}", 
			orderId, request.getStatus(), currentUser.getId());
		
		return mapToAdminOrderResponse(savedOrder);
	}

	private BigDecimal calculateBillDiscounts(List<ProductVariant> variants, 
											  Map<UUID, Integer> variantQuantityMap,
											  Map<UUID, Discount> productDiscountMap) {
		BigDecimal totalDiscountAmount = BigDecimal.ZERO;
		
		for (ProductVariant variant : variants) {
			if (productDiscountMap.containsKey(variant.getProduct().getId())) {
				Integer requestedQuantity = variantQuantityMap.get(variant.getId());
				BigDecimal itemTotal = variant.getPrice().multiply(BigDecimal.valueOf(requestedQuantity));
				
				Discount discount = productDiscountMap.get(variant.getProduct().getId());
				BigDecimal itemDiscountAmount = discount.calculateDiscountAmount(itemTotal);
				totalDiscountAmount = totalDiscountAmount.add(itemDiscountAmount);
			}
		}
		
		return totalDiscountAmount;
	}

	private Address validateAddressOwnership(UUID addressId, UUID userId) {
		Address address = addressRepository.findByIdAndNotDeleted(addressId)
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

	private BigDecimal calculateOrderDiscounts(List<ProductVariant> variants, 
											   Map<UUID, Integer> variantQuantityMap,
											   Map<UUID, Discount> productDiscountMap) {
		BigDecimal totalDiscountAmount = BigDecimal.ZERO;
		
		for (ProductVariant variant : variants) {
			if (productDiscountMap.containsKey(variant.getProduct().getId())) {
				Integer requestedQuantity = variantQuantityMap.get(variant.getId());
				BigDecimal itemTotal = variant.getPrice().multiply(BigDecimal.valueOf(requestedQuantity));
				
				Discount discount = productDiscountMap.get(variant.getProduct().getId());
				BigDecimal itemDiscountAmount = discount.calculateDiscountAmount(itemTotal);
				totalDiscountAmount = totalDiscountAmount.add(itemDiscountAmount);
			}
		}
		
		return totalDiscountAmount;
	}

	private CreateOrderResponse mapToCreateOrderResponse(Order order) {
		List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
			.map(this::mapToOrderItemResponse)
			.collect(Collectors.toList());

		// Order number should already be set for new orders
		String orderNumber = order.getOrderNumber();
		if (orderNumber == null) {
			orderNumber = generateOrderNumber();
			order.setOrderNumber(orderNumber);
			orderRepository.save(order);
		}

		return CreateOrderResponse.builder()
			.orderId(order.getId())
			.orderNumber(orderNumber)
			.addressId(order.getAddress().getId())
			.originalAmount(order.getOriginalAmount())
			.discountAmount(order.getDiscountAmount())
			.deliveryFee(order.getDeliveryFee())
			.totalAmount(order.getTotalAmount())
			.status(order.getStatus())
			.orderItems(orderItemResponses)
			.createdAt(order.getCreatedAt())
			.build();
	}

	private OrderResponse mapToOrderResponse(Order order) {
		List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream().map(this::mapToOrderItemResponse)
				.collect(Collectors.toList());

		// Generate order number if it doesn't exist (for existing orders)
		String orderNumber = order.getOrderNumber();
		if (orderNumber == null) {
			orderNumber = generateOrderNumber();
			order.setOrderNumber(orderNumber);
			orderRepository.save(order);
		}

		return OrderResponse.builder().id(order.getId()).orderNumber(orderNumber)
				.addressId(order.getAddress().getId()).userId(order.getUser().getId())
				.userEmail(order.getUser().getEmail()).userFirstName(order.getUser().getFirstName())
				.userLastName(order.getUser().getLastName()).originalAmount(order.getOriginalAmount())
				.discountAmount(order.getDiscountAmount()).deliveryFee(order.getDeliveryFee())
				.totalAmount(order.getTotalAmount()).status(order.getStatus())
				.orderItems(orderItemResponses).createdAt(order.getCreatedAt())
				.updatedAt(order.getUpdatedAt()).build();
	}

	private PaginatedOrdersResponse mapToPaginatedOrdersResponse(Page<Order> orderPage) {
		List<OrderResponse> orderResponses = orderPage.getContent().stream()
				.map(this::mapToOrderResponse)
				.collect(Collectors.toList());

		return PaginatedOrdersResponse.builder()
				.orders(orderResponses)
				.currentPage(orderPage.getNumber())
				.totalPages(orderPage.getTotalPages())
				.totalElements(orderPage.getTotalElements())
				.hasNext(orderPage.hasNext())
				.hasPrevious(orderPage.hasPrevious())
				.build();
	}

	private OrderItemResponse mapToOrderItemResponse(OrderItem orderItem) {
		BigDecimal totalPrice = orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
		
		// Get variant-specific images first, fallback to product images
		List<ProductImageResponse> images = getVariantImages(orderItem.getVariant());
		
		return OrderItemResponse.builder().id(orderItem.getId()).variantId(orderItem.getVariant().getId())
				.sku(orderItem.getVariant().getSku()).attributes(orderItem.getVariant().getAttributes())
				.productName(orderItem.getVariant().getProduct().getName()).quantity(orderItem.getQuantity())
				.unitPrice(orderItem.getUnitPrice()).totalPrice(totalPrice)
				.images(images)
				.build();
	}

	private List<ProductImageResponse> getVariantImages(com.charbel.ecommerce.product.entity.ProductVariant variant) {
		// First try to get variant-specific images
		List<ProductImage> variantImages = productImageRepository.findByVariantId(variant.getId());
		
		// If no variant-specific images, get product images (where variantId is null)
		if (variantImages.isEmpty()) {
			variantImages = productImageRepository.findByProductIdAndVariantIdIsNull(variant.getProduct().getId());
		}
		
		// Sort images: primary first, then by sort order
		return variantImages.stream()
				.sorted((a, b) -> {
					// Primary images first, then by sort order
					if (a.getIsPrimary() && !b.getIsPrimary()) return -1;
					if (!a.getIsPrimary() && b.getIsPrimary()) return 1;
					return a.getSortOrder().compareTo(b.getSortOrder());
				})
				.map(this::mapToProductImageResponse)
				.collect(Collectors.toList());
	}

	private ProductImageResponse mapToProductImageResponse(ProductImage image) {
		return ProductImageResponse.builder()
				.id(image.getId())
				.imageUrl(image.getImageUrl())
				.altText(image.getAltText())
				.isPrimary(image.getIsPrimary())
				.sortOrder(image.getSortOrder())
				.build();
	}

	private AdminOrderResponseDTO mapToAdminOrderResponse(Order order) {
		List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
				.map(this::mapToOrderItemResponse)
				.collect(Collectors.toList());

		// Generate order number if it doesn't exist
		String orderNumber = order.getOrderNumber();
		if (orderNumber == null) {
			orderNumber = generateOrderNumber();
			order.setOrderNumber(orderNumber);
			orderRepository.save(order);
		}

		// Map user information
		AdminOrderResponseDTO.UserInfo userInfo = AdminOrderResponseDTO.UserInfo.builder()
				.id(order.getUser().getId())
				.email(order.getUser().getEmail())
				.firstName(order.getUser().getFirstName())
				.lastName(order.getUser().getLastName())
				.role(order.getUser().getRole())
				.createdAt(order.getUser().getCreatedAt())
				.updatedAt(order.getUser().getUpdatedAt())
				.build();

		// Map address information
		AdminOrderResponseDTO.AddressInfo addressInfo = AdminOrderResponseDTO.AddressInfo.builder()
				.id(order.getAddress().getId())
				.street(order.getAddress().getStreet())
				.city(order.getAddress().getCity())
				.state(order.getAddress().getState())
				.zipCode(order.getAddress().getZipCode())
				.country(order.getAddress().getCountry())
				.isDefault(order.getAddress().getIsDefault())
				.createdAt(order.getAddress().getCreatedAt())
				.updatedAt(order.getAddress().getUpdatedAt())
				.build();

		return AdminOrderResponseDTO.builder()
				.id(order.getId())
				.orderNumber(orderNumber)
				.originalAmount(order.getOriginalAmount())
				.discountAmount(order.getDiscountAmount())
				.deliveryFee(order.getDeliveryFee())
				.totalAmount(order.getTotalAmount())
				.status(order.getStatus())
				.orderItems(orderItemResponses)
				.createdAt(order.getCreatedAt())
				.updatedAt(order.getUpdatedAt())
				.user(userInfo)
				.address(addressInfo)
				.build();
	}

	private PaginatedAdminOrdersResponse mapToPaginatedAdminOrdersResponse(Page<Order> orderPage) {
		List<AdminOrderResponseDTO> adminOrderResponses = orderPage.getContent().stream()
				.map(this::mapToAdminOrderResponse)
				.collect(Collectors.toList());

		return PaginatedAdminOrdersResponse.builder()
				.orders(adminOrderResponses)
				.currentPage(orderPage.getNumber())
				.totalPages(orderPage.getTotalPages())
				.totalElements(orderPage.getTotalElements())
				.pageSize(orderPage.getSize())
				.hasNext(orderPage.hasNext())
				.hasPrevious(orderPage.hasPrevious())
				.build();
	}
}
