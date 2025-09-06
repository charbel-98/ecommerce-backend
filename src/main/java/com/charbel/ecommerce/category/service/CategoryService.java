package com.charbel.ecommerce.category.service;

import com.charbel.ecommerce.category.dto.CategoryResponse;
import com.charbel.ecommerce.category.dto.CreateCategoryRequest;
import com.charbel.ecommerce.category.entity.Category;
import com.charbel.ecommerce.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

	private final CategoryRepository categoryRepository;

	@Transactional
	public CategoryResponse createCategory(CreateCategoryRequest request) {
		log.info("Creating new category: {}", request.getName());

		if (categoryRepository.existsByName(request.getName())) {
			throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists");
		}

		if (categoryRepository.existsBySlug(request.getSlug())) {
			throw new IllegalArgumentException("Category with slug '" + request.getSlug() + "' already exists");
		}

		Integer level = 0;
		if (request.getParentId() != null) {
			Category parentCategory = categoryRepository.findById(request.getParentId())
					.orElseThrow(() -> new IllegalArgumentException(
							"Parent category with ID '" + request.getParentId() + "' not found"));
			level = parentCategory.getLevel() + 1;
		}

		Category category = Category.builder().name(request.getName()).slug(request.getSlug())
				.description(request.getDescription()).parentId(request.getParentId()).level(level)
				.sortOrder(request.getSortOrder()).isActive(true).build();

		Category savedCategory = categoryRepository.save(category);
		log.info("Category created successfully with ID: {}", savedCategory.getId());

		return mapToCategoryResponse(savedCategory);
	}

	@Transactional(readOnly = true)
	public List<CategoryResponse> getAllCategories() {
		log.info("Fetching all active categories");
		return categoryRepository.findByIsActiveTrueOrderBySortOrderAscNameAsc().stream()
				.map(this::mapToCategoryResponse).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<CategoryResponse> getLeafCategories() {
		log.info("Fetching leaf categories");
		List<Category> allCategories = categoryRepository.findByIsActiveTrueOrderBySortOrderAscNameAsc();

		List<UUID> parentIds = allCategories.stream().map(Category::getParentId).filter(parentId -> parentId != null)
				.distinct().collect(Collectors.toList());

		return allCategories.stream().filter(category -> !parentIds.contains(category.getId()))
				.map(this::mapToCategoryResponse).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public CategoryResponse getCategoryById(UUID id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Category with ID '" + id + "' not found"));
		return mapToCategoryResponse(category);
	}

	@Transactional(readOnly = true)
	public boolean isLeafCategory(UUID categoryId) {
		List<Category> children = categoryRepository.findByParentId(categoryId);
		return children.isEmpty();
	}

	@Transactional(readOnly = true)
	public void validateLeafCategory(UUID categoryId) {
		if (!isLeafCategory(categoryId)) {
			Category category = categoryRepository.findById(categoryId)
					.orElseThrow(() -> new IllegalArgumentException("Category not found"));
			throw new IllegalArgumentException("Category '" + category.getName()
					+ "' is not a leaf category. Products must be assigned to leaf categories only.");
		}
	}

	private CategoryResponse mapToCategoryResponse(Category category) {
		return CategoryResponse.builder().id(category.getId()).name(category.getName()).slug(category.getSlug())
				.description(category.getDescription()).parentId(category.getParentId()).level(category.getLevel())
				.sortOrder(category.getSortOrder()).isActive(category.getIsActive()).createdAt(category.getCreatedAt())
				.updatedAt(category.getUpdatedAt()).build();
	}
}
