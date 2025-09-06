package com.charbel.ecommerce.brand.controller;

import com.charbel.ecommerce.brand.dto.BrandResponse;
import com.charbel.ecommerce.brand.dto.CreateBrandRequest;
import com.charbel.ecommerce.brand.service.BrandService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Brands", description = "Brand management endpoints")
public class BrandController {

	private final BrandService brandService;

	// Public endpoints for customers
	@GetMapping("/brands")
	@Operation(summary = "Get all active brands", description = "Returns all active brands for customer use")
	public ResponseEntity<List<BrandResponse>> getActiveBrands() {
		log.info("Fetching all active brands");
		List<BrandResponse> brands = brandService.getActiveBrands();
		return ResponseEntity.ok(brands);
	}

	@GetMapping("/brands/{slug}")
	@Operation(summary = "Get brand by slug", description = "Returns brand details by slug")
	public ResponseEntity<BrandResponse> getBrandBySlug(@PathVariable String slug) {
		log.info("Fetching brand by slug: {}", slug);
		BrandResponse brand = brandService.getBrandBySlug(slug);
		return ResponseEntity.ok(brand);
	}

	// Admin-only endpoints
	@GetMapping("/admin/brands")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Get all brands", description = "Returns all brands (including inactive). Admin only.", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<List<BrandResponse>> getAllBrands() {
		log.info("Admin fetching all brands");
		List<BrandResponse> brands = brandService.getAllBrands();
		return ResponseEntity.ok(brands);
	}

	@PostMapping("/admin/brands")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Create a new brand", description = "Creates a new brand. Admin only.", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<BrandResponse> createBrand(@Valid @RequestBody CreateBrandRequest request) {
		log.info("Admin creating new brand: {}", request.getName());
		BrandResponse brand = brandService.createBrand(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(brand);
	}

	@PutMapping("/admin/brands/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Update a brand", description = "Updates an existing brand. Admin only.", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<BrandResponse> updateBrand(@PathVariable UUID id,
			@Valid @RequestBody CreateBrandRequest request) {
		log.info("Admin updating brand with id: {}", id);
		BrandResponse brand = brandService.updateBrand(id, request);
		return ResponseEntity.ok(brand);
	}

	@DeleteMapping("/admin/brands/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Delete a brand", description = "Deletes a brand. Admin only.", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Void> deleteBrand(@PathVariable UUID id) {
		log.info("Admin deleting brand with id: {}", id);
		brandService.deleteBrand(id);
		return ResponseEntity.noContent().build();
	}
}
