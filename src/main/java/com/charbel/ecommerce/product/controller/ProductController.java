package com.charbel.ecommerce.product.controller;

import com.charbel.ecommerce.product.dto.CreateProductRequest;
import com.charbel.ecommerce.product.dto.ProductResponse;
import com.charbel.ecommerce.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

	private final ProductService productService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
		summary = "Create a new product",
		description = "Creates a new product with variants. Admin only.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
		log.info("Admin creating new product: {}", request.getName());
		ProductResponse response = productService.createProduct(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

}