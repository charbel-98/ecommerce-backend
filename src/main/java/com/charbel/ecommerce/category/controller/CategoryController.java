package com.charbel.ecommerce.category.controller;

import com.charbel.ecommerce.category.dto.CategoryResponse;
import com.charbel.ecommerce.category.dto.CreateCategoryRequest;
import com.charbel.ecommerce.category.service.CategoryService;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        log.info("Creating new category: {}", request.getName());
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        log.info("Fetching all categories");
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/leaf")
    public ResponseEntity<List<CategoryResponse>> getLeafCategories() {
        log.info("Fetching leaf categories");
        List<CategoryResponse> leafCategories = categoryService.getLeafCategories();
        return ResponseEntity.ok(leafCategories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable UUID id) {
        log.info("Fetching category with ID: {}", id);
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }
}