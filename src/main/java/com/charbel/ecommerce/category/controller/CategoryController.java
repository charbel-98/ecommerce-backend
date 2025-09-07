package com.charbel.ecommerce.category.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.charbel.ecommerce.category.dto.CategoryResponse;
import com.charbel.ecommerce.category.dto.CreateCategoryRequest;
import com.charbel.ecommerce.category.dto.PaginatedCategoriesResponse;
import com.charbel.ecommerce.category.dto.UpdateCategoryRequest;
import com.charbel.ecommerce.category.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

	private final CategoryService categoryService;

	// Admin endpoints
	@PostMapping(value = "/admin/categories", consumes = "multipart/form-data")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<CategoryResponse> createCategory(@Valid @ModelAttribute CreateCategoryRequest request) {
		log.info("Creating new category: {}", request.getName());
		CategoryResponse response = categoryService.createCategory(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping(value = "/admin/categories/{id}", consumes = "multipart/form-data")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<CategoryResponse> updateCategory(@PathVariable UUID id,
			@Valid @ModelAttribute UpdateCategoryRequest request) {
		log.info("Updating category with ID: {}", id);
		CategoryResponse response = categoryService.updateCategory(id, request);
		return ResponseEntity.ok(response);
	}

	// Public endpoints
	@GetMapping("/categories")
	public ResponseEntity<List<CategoryResponse>> getAllCategories() {
		log.info("Fetching all categories");
		List<CategoryResponse> categories = categoryService.getAllCategories();
		return ResponseEntity.ok(categories);
	}

	@GetMapping("/categories/leaf")
	public ResponseEntity<List<CategoryResponse>> getLeafCategories() {
		log.info("Fetching leaf categories");
		List<CategoryResponse> leafCategories = categoryService.getLeafCategories();
		return ResponseEntity.ok(leafCategories);
	}

	@GetMapping("/categories/{id}")
	public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable UUID id) {
		log.info("Fetching category with ID: {}", id);
		CategoryResponse category = categoryService.getCategoryById(id);
		return ResponseEntity.ok(category);
	}

	@GetMapping("/categories/with-products")
	public ResponseEntity<PaginatedCategoriesResponse> getLeafCategoriesWithProducts(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		log.info("Fetching leaf categories with products - page: {}, size: {}", page, size);
		PaginatedCategoriesResponse response = categoryService.getLeafCategoriesWithProducts(page, size);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/categories/{id}/subtree")
	public ResponseEntity<CategoryResponse> getCategorySubtree(@PathVariable UUID id) {
		log.info("Fetching category subtree for ID: {}", id);
		CategoryResponse subtree = categoryService.getCategorySubtree(id);
		return ResponseEntity.ok(subtree);
	}
}
