package com.charbel.ecommerce.product.service;

import com.charbel.ecommerce.ai.service.ColorVariantImageService;
import com.charbel.ecommerce.category.service.CategoryService;
import com.charbel.ecommerce.common.enums.GenderType;
import com.charbel.ecommerce.event.entity.Discount;
import com.charbel.ecommerce.event.entity.Event;
import com.charbel.ecommerce.product.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import com.charbel.ecommerce.product.entity.Product;
import com.charbel.ecommerce.product.entity.ProductImage;
import com.charbel.ecommerce.product.entity.ProductVariant;
import com.charbel.ecommerce.product.repository.ProductImageRepository;
import com.charbel.ecommerce.product.repository.ProductRepository;
import com.charbel.ecommerce.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductVariantRepository productVariantRepository;
	private final ProductImageRepository productImageRepository;
	private final CategoryService categoryService;
	private final ColorVariantImageService colorVariantImageService;
	private final ObjectMapper objectMapper;

	@Transactional
	public ProductResponse createProduct(CreateProductRequest request) {
		log.info("Creating new product with AI variants: {}", request.getName());

		try {
			// VALIDATE FIRST - before any expensive operations
			validateProductRequest(request);

			// Extract unique colors from variants
			List<String> uniqueColors = extractUniqueColorsFromVariants(request.getVariants());

			// Generate AI variant images using the new service
			byte[] originalImageData = request.getImage().getBytes();
			String productType = extractProductTypeFromName(request.getName());
			Map<String, String> variantImageUrls = colorVariantImageService
					.generateColorVariantImages(originalImageData, productType, uniqueColors);

			log.info("Generated {} variant images: {}", variantImageUrls.size(), variantImageUrls.keySet());

			return createProductWithGeneratedImages(request, variantImageUrls);

		} catch (Exception e) {
			log.error("Failed to create product with AI variants", e);
			throw new RuntimeException("Failed to create product with AI variants: " + e.getMessage(), e);
		}
	}

	@Transactional
	public ProductResponse createProductWithAIVariants(MultipartFile image, String name, String description,
			Integer basePrice, UUID brandId, UUID categoryId, String genderStr, String variantsJson,
			String metadataJson) {

		log.info("Creating product with AI-generated variants: {}", name);

		try {
			// Parse gender
			GenderType gender = GenderType.valueOf(genderStr.toUpperCase());

			// Parse variants JSON
			List<ProductVariantRequest> variants = objectMapper.readValue(variantsJson,
					new TypeReference<List<ProductVariantRequest>>() {
					});

			// Parse metadata JSON if provided
			Map<String, Object> metadata = null;
			if (metadataJson != null && !metadataJson.trim().isEmpty()) {
				metadata = objectMapper.readValue(metadataJson, new TypeReference<Map<String, Object>>() {
				});
			}

			// Create a CreateProductRequest to reuse the existing logic
			CreateProductRequest request = new CreateProductRequest();
			request.setImage(image);
			request.setName(name);
			request.setDescription(description);
			request.setBasePrice(basePrice);
			request.setBrandId(brandId);
			request.setCategoryId(categoryId);
			request.setGender(gender);
			request.setVariants(variants);
			request.setMetadata(metadata);

			return createProduct(request);

		} catch (Exception e) {
			log.error("Failed to create product with AI variants", e);
			throw new RuntimeException("Failed to create product with AI variants: " + e.getMessage(), e);
		}
	}

	private void validateProductRequest(CreateProductRequest request) {
		// Validate SKU uniqueness for all variants
		Set<String> requestedSkus = request.getVariants().stream().map(ProductVariantRequest::getSku)
				.collect(Collectors.toSet());

		if (requestedSkus.size() != request.getVariants().size()) {
			throw new IllegalArgumentException("Duplicate SKUs found in request variants");
		}

		Set<String> existingSkus = productVariantRepository.findExistingSkus(requestedSkus);
		if (!existingSkus.isEmpty()) {
			throw new IllegalArgumentException("SKUs already exist: " + String.join(", ", existingSkus));
		}

		if (request.getCategoryId() != null) {
			categoryService.validateLeafCategory(request.getCategoryId());
		}
	}

	private ProductResponse createProductWithGeneratedImages(CreateProductRequest request,
			Map<String, String> variantImageUrls) {

		// Create the product
		Product product = Product.builder().name(request.getName()).description(request.getDescription())
				.basePrice(request.getBasePrice()).brandId(request.getBrandId()).categoryId(request.getCategoryId())
				.gender(request.getGender()).metadata(request.getMetadata()).status(Product.ProductStatus.ACTIVE)
				.build();

		Product savedProduct = productRepository.save(product);

		// Create variants
		List<ProductVariant> variants = request.getVariants().stream()
				.map(variantRequest -> ProductVariant.builder().product(savedProduct).sku(variantRequest.getSku())
						.attributes(variantRequest.getAttributes()).price(variantRequest.getPrice())
						.stock(variantRequest.getStock()).build())
				.collect(Collectors.toList());

		List<ProductVariant> savedVariants = productVariantRepository.saveAll(variants);
		savedProduct.setVariants(savedVariants);

		// Create product and variant images
		List<ProductImage> productImages = new ArrayList<>();

		// Add main product image (enhanced original)
		String mainImageUrl = variantImageUrls.get("enhancedOriginal");
		if (mainImageUrl != null) {
			ProductImage mainImage = ProductImage.builder().productId(savedProduct.getId()).variantId(null)
					.imageUrl(mainImageUrl).altText("Enhanced product image").isPrimary(true).sortOrder(0).build();
			productImages.add(mainImage);
		}

		// Map variant images to variants by color
		for (ProductVariant variant : savedVariants) {
			String variantColor = extractColorFromVariant(variant);
			String variantImageUrl = variantImageUrls.get(variantColor);

			// Fallback to main image if variant image not found
			if (variantImageUrl == null) {
				variantImageUrl = mainImageUrl;
			}

			if (variantImageUrl != null) {
				ProductImage variantImage = ProductImage.builder().productId(savedProduct.getId())
						.variantId(variant.getId()).imageUrl(variantImageUrl)
						.altText("Variant image for " + variantColor).isPrimary(true).sortOrder(0).build();
				productImages.add(variantImage);
			}
		}

		if (!productImages.isEmpty()) {
			productImageRepository.saveAll(productImages);
		}

		log.info("Product with AI variants created successfully with ID: {}", savedProduct.getId());
		return mapToProductResponse(savedProduct);
	}

	private List<String> extractUniqueColorsFromVariants(List<ProductVariantRequest> variants) {
		Set<String> uniqueColors = new HashSet<>();

		for (ProductVariantRequest variant : variants) {
			Object colorValue = variant.getAttributes().get("color");
			if (colorValue != null) {
				uniqueColors.add(colorValue.toString().toLowerCase().trim());
			}
		}

		log.debug("Extracted {} unique colors: {}", uniqueColors.size(), uniqueColors);
		return new ArrayList<>(uniqueColors);
	}

	private String extractProductTypeFromName(String productName) {
		// Simple extraction - in production you might use NLP or predefined mappings
		String lowerName = productName.toLowerCase();

		if (lowerName.contains("shirt"))
			return "shirt";
		if (lowerName.contains("polo"))
			return "polo";
		if (lowerName.contains("jacket"))
			return "jacket";
		if (lowerName.contains("sweater") || lowerName.contains("pullover"))
			return "sweater";
		if (lowerName.contains("pants") || lowerName.contains("trousers"))
			return "pants";
		if (lowerName.contains("jeans"))
			return "jeans";
		if (lowerName.contains("dress"))
			return "dress";
		if (lowerName.contains("hoodie"))
			return "hoodie";

		// Default to generic term
		return "clothing item";
	}

	private String extractColorFromVariant(ProductVariant variant) {
		Object colorValue = variant.getAttributes().get("color");
		return colorValue != null ? colorValue.toString().toLowerCase().trim() : "default";
	}

	public List<LowStockResponse> getLowStockProducts() {
		log.info("Fetching low stock products");
		List<ProductVariant> lowStockVariants = productVariantRepository.findLowStockVariants(5);

		return lowStockVariants.stream().map(this::mapToLowStockResponse).collect(Collectors.toList());
	}

	public List<LowStockResponse> getLowStockVariants() {
		log.info("Fetching low stock variants");
		List<ProductVariant> lowStockVariants = productVariantRepository.findLowStockVariants(5);

		return lowStockVariants.stream().map(this::mapToLowStockResponse).collect(Collectors.toList());
	}

	@Transactional
	public AddStockResponse addStockToVariants(AddStockRequest request) {
		log.info("Adding stock to {} variants", request.getStockUpdates().size());

		List<UUID> variantIds = request.getStockUpdates().stream().map(VariantStockUpdate::getVariantId)
				.collect(Collectors.toList());

		Map<UUID, ProductVariant> variantMap = productVariantRepository.findAllById(variantIds).stream()
				.collect(Collectors.toMap(ProductVariant::getId, Function.identity()));

		List<VariantStockUpdateResult> results = request.getStockUpdates().stream().map(update -> {
			ProductVariant variant = variantMap.get(update.getVariantId());
			if (variant == null) {
				throw new EntityNotFoundException("Variant not found with ID: " + update.getVariantId());
			}

			Integer previousStock = variant.getStock();
			Integer newStock = previousStock + update.getStockToAdd();
			variant.setStock(newStock);

			return VariantStockUpdateResult.builder().variantId(variant.getId()).sku(variant.getSku())
					.previousStock(previousStock).newStock(newStock).stockAdded(update.getStockToAdd()).build();
		}).collect(Collectors.toList());

		productVariantRepository.saveAll(variantMap.values());

		log.info("Successfully updated stock for {} variants", results.size());
		return AddStockResponse.builder().updatedVariantsCount(results.size()).results(results).build();
	}

	private ProductResponse mapToProductResponse(Product product) {
		// Get product images (not variant-specific)
		List<String> productImageUrls = productImageRepository.findByProductIdAndVariantIdIsNull(product.getId())
				.stream().map(ProductImage::getImageUrl).collect(Collectors.toList());

		// Use the existing fromEntity method which handles brand and category mapping
		ProductResponse response = ProductResponse.fromEntity(product);

		// Override imageUrls with the product-specific images
		response.setImageUrls(productImageUrls);

		// Get active discount information
		response.setDiscount(getActiveDiscountForProduct(product.getId()));

		return response;
	}

	private ProductVariantResponse mapToVariantResponse(ProductVariant variant) {
		// Get variant-specific images
		List<String> variantImageUrls = productImageRepository.findByVariantId(variant.getId()).stream()
				.map(ProductImage::getImageUrl).collect(Collectors.toList());

		return ProductVariantResponse.builder().id(variant.getId()).sku(variant.getSku())
				.attributes(variant.getAttributes()).price(variant.getPrice()).stock(variant.getStock())
				.imageUrls(variantImageUrls).createdAt(variant.getCreatedAt()).updatedAt(variant.getUpdatedAt())
				.build();
	}

	private LowStockResponse mapToLowStockResponse(ProductVariant variant) {
		return LowStockResponse.builder().variantId(variant.getId()).sku(variant.getSku())
				.attributes(variant.getAttributes()).stock(variant.getStock()).productId(variant.getProduct().getId())
				.productName(variant.getProduct().getName()).productDescription(variant.getProduct().getDescription())
				.build();
	}

	public Page<ProductResponse> getProducts(Pageable pageable) {
		log.info("Fetching paginated products");
		Page<Product> products = productRepository.findAllProductsWithVariants(pageable);
		return products.map(this::mapToProductResponse);
	}

	public Page<ProductResponse> getProductsByEventId(UUID eventId, Pageable pageable) {
		log.info("Fetching products for event ID: {}", eventId);
		Page<Product> products = productRepository.findProductsByEventId(eventId, pageable);
		return products.map(this::mapToProductResponse);
	}

	@Transactional
	public ProductResponse disableProduct(UUID productId) {
		log.info("Disabling product with ID: {}", productId);

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));

		product.setStatus(Product.ProductStatus.INACTIVE);
		Product savedProduct = productRepository.save(product);

		log.info("Product disabled successfully with ID: {}", productId);
		return mapToProductResponse(savedProduct);
	}

	@Transactional(readOnly = true)
	public Page<ProductResponse> getProductsByBrandSlug(String brandSlug, Pageable pageable) {
		log.info("Fetching products by brand slug: {} with page: {}, size: {}", brandSlug, pageable.getPageNumber(),
				pageable.getPageSize());

		Page<Product> products = productRepository.findProductsByBrandSlug(brandSlug, pageable);
		return products.map(this::mapToProductResponse);
	}

	@Transactional(readOnly = true)
	public Page<ProductResponse> getProductsByCategoryId(UUID categoryId, Pageable pageable) {
		log.info("Fetching products by category ID: {} with page: {}, size: {}", categoryId, pageable.getPageNumber(),
				pageable.getPageSize());

		Page<Product> products = productRepository.findProductsByCategoryId(categoryId, pageable);
		return products.map(this::mapToProductResponse);
	}

	private DiscountInfo getActiveDiscountForProduct(UUID productId) {
		LocalDateTime now = LocalDateTime.now();
		List<Event> activeEvents = productRepository.findActiveEventsWithDiscountsForProduct(productId, now);

		if (activeEvents.isEmpty()) {
			return null;
		}

		// Return the first active event with discounts (you might want to prioritize by discount value)
		Event event = activeEvents.get(0);
		Discount discount = event.getDiscounts().stream()
			.findFirst()
			.orElse(null);

		if (discount == null) {
			return null;
		}

		return DiscountInfo.builder()
			.eventId(event.getId())
			.eventName(event.getName())
			.discountId(discount.getId())
			.type(discount.getType())
			.value(discount.getValue())
			.minPurchaseAmount(discount.getMinPurchaseAmount())
			.maxDiscountAmount(discount.getMaxDiscountAmount())
			.eventStartDate(event.getStartDate())
			.eventEndDate(event.getEndDate())
			.build();
	}
}
