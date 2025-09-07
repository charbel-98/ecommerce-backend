package com.charbel.ecommerce.product.service;

import com.charbel.ecommerce.event.entity.Discount;
import com.charbel.ecommerce.event.entity.Event;
import com.charbel.ecommerce.product.dto.DiscountInfo;
import com.charbel.ecommerce.product.dto.ProductResponse;
import com.charbel.ecommerce.product.dto.ProductVariantResponse;
import com.charbel.ecommerce.product.entity.Product;
import com.charbel.ecommerce.product.entity.ProductImage;
import com.charbel.ecommerce.product.entity.ProductVariant;
import com.charbel.ecommerce.product.repository.ProductImageRepository;
import com.charbel.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductResponseMapper {

	private final ProductImageRepository productImageRepository;
	private final ProductRepository productRepository;

	public ProductResponse mapToProductResponse(Product product) {
		// Get product images (not variant-specific)
		List<String> productImageUrls = productImageRepository.findByProductIdAndVariantIdIsNull(product.getId())
				.stream().map(ProductImage::getImageUrl).collect(Collectors.toList());

		// Use the existing fromEntity method which handles brand and category mapping
		ProductResponse response = ProductResponse.fromEntity(product);

		// Override imageUrls with the product-specific images
		response.setImageUrls(productImageUrls);

		// Map variants with their specific images using the custom method
		if (product.getVariants() != null) {
			List<ProductVariantResponse> variantResponses = product.getVariants().stream()
					.map(this::mapToVariantResponse)
					.collect(Collectors.toList());
			response.setVariants(variantResponses);
		}

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