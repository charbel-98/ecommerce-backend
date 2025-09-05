package com.charbel.ecommerce.product.service;

import com.charbel.ecommerce.category.service.CategoryService;
import com.charbel.ecommerce.product.dto.*;
import jakarta.persistence.EntityNotFoundException;
import com.charbel.ecommerce.product.entity.Product;
import com.charbel.ecommerce.product.entity.ProductVariant;
import com.charbel.ecommerce.product.repository.ProductRepository;
import com.charbel.ecommerce.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	private final CategoryService categoryService;

	@Transactional
	public ProductResponse createProduct(CreateProductRequest request) {
		log.info("Creating new product: {}", request.getName());

		// Validate SKU uniqueness for all variants
		Set<String> requestedSkus = request.getVariants().stream()
				.map(ProductVariantRequest::getSku)
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

		Product product = Product.builder()
				.name(request.getName())
				.description(request.getDescription())
				.basePrice(request.getBasePrice())
				.brandId(request.getBrandId())
				.categoryId(request.getCategoryId())
				.gender(request.getGender())
				.metadata(request.getMetadata())
				.status(Product.ProductStatus.ACTIVE)
				.build();

		Product savedProduct = productRepository.save(product);

		List<ProductVariant> variants = request.getVariants().stream()
				.map(variantRequest -> ProductVariant.builder()
						.product(savedProduct)
						.sku(variantRequest.getSku())
						.attributes(variantRequest.getAttributes())
						.price(variantRequest.getPrice())
						.stock(variantRequest.getStock())
						.build())
				.collect(Collectors.toList());

		List<ProductVariant> savedVariants = productVariantRepository.saveAll(variants);
		savedProduct.setVariants(savedVariants);

		log.info("Product created successfully with ID: {}", savedProduct.getId());
		return mapToProductResponse(savedProduct);
	}

	public List<LowStockResponse> getLowStockProducts() {
		log.info("Fetching low stock products");
		List<ProductVariant> lowStockVariants = productVariantRepository.findLowStockVariants(5);
		
		return lowStockVariants.stream()
				.map(this::mapToLowStockResponse)
				.collect(Collectors.toList());
	}

	public List<LowStockResponse> getLowStockVariants() {
		log.info("Fetching low stock variants");
		List<ProductVariant> lowStockVariants = productVariantRepository.findLowStockVariants(5);
		
		return lowStockVariants.stream()
				.map(this::mapToLowStockResponse)
				.collect(Collectors.toList());
	}

	@Transactional
	public AddStockResponse addStockToVariants(AddStockRequest request) {
		log.info("Adding stock to {} variants", request.getStockUpdates().size());
		
		List<UUID> variantIds = request.getStockUpdates().stream()
				.map(VariantStockUpdate::getVariantId)
				.collect(Collectors.toList());

		Map<UUID, ProductVariant> variantMap = productVariantRepository.findAllById(variantIds)
				.stream()
				.collect(Collectors.toMap(ProductVariant::getId, Function.identity()));

		List<VariantStockUpdateResult> results = request.getStockUpdates().stream()
				.map(update -> {
					ProductVariant variant = variantMap.get(update.getVariantId());
					if (variant == null) {
						throw new EntityNotFoundException("Variant not found with ID: " + update.getVariantId());
					}

					Integer previousStock = variant.getStock();
					Integer newStock = previousStock + update.getStockToAdd();
					variant.setStock(newStock);

					return VariantStockUpdateResult.builder()
							.variantId(variant.getId())
							.sku(variant.getSku())
							.previousStock(previousStock)
							.newStock(newStock)
							.stockAdded(update.getStockToAdd())
							.build();
				})
				.collect(Collectors.toList());

		productVariantRepository.saveAll(variantMap.values());

		log.info("Successfully updated stock for {} variants", results.size());
		return AddStockResponse.builder()
				.updatedVariantsCount(results.size())
				.results(results)
				.build();
	}

	private ProductResponse mapToProductResponse(Product product) {
		List<ProductVariantResponse> variantResponses = product.getVariants().stream()
				.map(this::mapToVariantResponse)
				.collect(Collectors.toList());

		return ProductResponse.builder()
				.id(product.getId())
				.name(product.getName())
				.description(product.getDescription())
				.basePrice(product.getBasePrice())
				.status(product.getStatus())
				.variants(variantResponses)
				.createdAt(product.getCreatedAt())
				.updatedAt(product.getUpdatedAt())
				.build();
	}

	private ProductVariantResponse mapToVariantResponse(ProductVariant variant) {
		return ProductVariantResponse.builder()
				.id(variant.getId())
				.sku(variant.getSku())
				.attributes(variant.getAttributes())
				.price(variant.getPrice())
				.stock(variant.getStock())
				.createdAt(variant.getCreatedAt())
				.updatedAt(variant.getUpdatedAt())
				.build();
	}

	private LowStockResponse mapToLowStockResponse(ProductVariant variant) {
		return LowStockResponse.builder()
				.variantId(variant.getId())
				.sku(variant.getSku())
				.attributes(variant.getAttributes())
				.stock(variant.getStock())
				.productId(variant.getProduct().getId())
				.productName(variant.getProduct().getName())
				.productDescription(variant.getProduct().getDescription())
				.build();
	}

	public Page<ProductResponse> getProducts(Pageable pageable) {
		log.info("Fetching paginated products");
		Page<Product> products = productRepository.findAllProductsWithVariants(pageable);
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
}