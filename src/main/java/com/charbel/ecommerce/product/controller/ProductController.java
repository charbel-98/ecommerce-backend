package com.charbel.ecommerce.product.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.charbel.ecommerce.product.dto.AddStockRequest;
import com.charbel.ecommerce.product.dto.AddStockResponse;
import com.charbel.ecommerce.product.dto.CreateProductRequest;
import com.charbel.ecommerce.product.dto.LowStockResponse;
import com.charbel.ecommerce.product.dto.ProductResponse;
import com.charbel.ecommerce.product.service.ProductService;

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
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

	private final ProductService productService;

	// Admin endpoints
	@PostMapping("/admin/products")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Create a new product", description = "Creates a new product with variants. Admin only.", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
		log.info("Admin creating new product: {}", request.getName());
		ProductResponse response = productService.createProduct(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/admin/products/low-stock")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Get low stock products", description = "Returns products with stock less than 5. Admin only.", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<List<LowStockResponse>> getLowStockProducts() {
		log.info("Admin requesting low stock products");
		List<LowStockResponse> response = productService.getLowStockProducts();
		return ResponseEntity.ok(response);
	}

	@GetMapping("/admin/products/low-stock-variants")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Get low stock variants", description = "Returns product variants with stock less than 5. Admin only.", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<List<LowStockResponse>> getLowStockVariants() {
		log.info("Admin requesting low stock variants");
		List<LowStockResponse> response = productService.getLowStockVariants();
		return ResponseEntity.ok(response);
	}

	@PostMapping("/admin/products/add-stock")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Add stock to variants", description = "Adds stock to multiple product variants. Admin only.", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<AddStockResponse> addStockToVariants(@Valid @RequestBody AddStockRequest request) {
		log.info("Admin adding stock to {} variants", request.getStockUpdates().size());
		AddStockResponse response = productService.addStockToVariants(request);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/admin/products/{productId}/disable")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Disable a product", description = "Sets product status to INACTIVE. Admin only.", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<ProductResponse> disableProduct(@PathVariable UUID productId) {
		log.info("Admin disabling product with ID: {}", productId);
		ProductResponse response = productService.disableProduct(productId);
		return ResponseEntity.ok(response);
	}

	// Public endpoints
	@GetMapping("/products")
	@Operation(summary = "Get paginated products", description = "Returns a paginated list of products with their variants")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<Page<ProductResponse>> getProducts(@PageableDefault(size = 20) Pageable pageable) {
		log.info("Fetching products with pagination: page={}, size={}", pageable.getPageNumber(),
				pageable.getPageSize());
		Page<ProductResponse> response = productService.getProducts(pageable);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/events/{eventId}/products")
	@Operation(summary = "Get products by event", description = "Returns a paginated list of products associated with a specific event")
	public ResponseEntity<Page<ProductResponse>> getProductsByEvent(@PathVariable UUID eventId,
			@PageableDefault(size = 20) Pageable pageable) {
		log.info("Fetching products for event ID: {} with pagination: page={}, size={}", eventId,
				pageable.getPageNumber(), pageable.getPageSize());
		Page<ProductResponse> response = productService.getProductsByEventId(eventId, pageable);
		return ResponseEntity.ok(response);
	}

}
