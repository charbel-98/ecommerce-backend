package com.charbel.ecommerce.product.service;

import com.charbel.ecommerce.product.dto.*;
import com.charbel.ecommerce.product.entity.Product;
import com.charbel.ecommerce.product.entity.ProductVariant;
import com.charbel.ecommerce.product.repository.ProductRepository;
import com.charbel.ecommerce.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductVariantRepository productVariantRepository;

	@Transactional
	public ProductResponse createProduct(CreateProductRequest request) {
		log.info("Creating new product: {}", request.getName());

		Product product = Product.builder()
				.name(request.getName())
				.description(request.getDescription())
				.basePrice(request.getBasePrice())
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
}