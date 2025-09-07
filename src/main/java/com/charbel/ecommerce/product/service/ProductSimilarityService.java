package com.charbel.ecommerce.product.service;

import com.charbel.ecommerce.product.dto.SimilarProductResponse;
import com.charbel.ecommerce.product.entity.Product;
import com.charbel.ecommerce.product.entity.ProductImage;
import com.charbel.ecommerce.product.repository.ProductImageRepository;
import com.charbel.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSimilarityService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    private static final double GENDER_WEIGHT = 0.25;
    private static final double CATEGORY_WEIGHT = 0.20;
    private static final double BRAND_WEIGHT = 0.15;
    private static final double MATERIAL_WEIGHT = 0.15;
    private static final double SEASON_WEIGHT = 0.10;
    private static final double OCCASION_WEIGHT = 0.10;
    private static final double FIT_WEIGHT = 0.05;

    public List<SimilarProductResponse> findSimilarProducts(UUID productId, int limit) {
        log.info("Finding similar products for product ID: {} with limit: {}", productId, limit);
        
        Product targetProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        if (targetProduct.getStatus() != Product.ProductStatus.ACTIVE) {
            throw new RuntimeException("Cannot find similar products for inactive product");
        }

        List<Product> allActiveProducts = productRepository.findByStatusAndIdNot(
                Product.ProductStatus.ACTIVE, productId);

        List<SimilarProductResponse> similarProducts = allActiveProducts.stream()
                .map(product -> calculateSimilarityScore(targetProduct, product))
                .filter(response -> response.getSimilarityScore() > 0.0) // Only include products with some similarity
                .sorted((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore())) // Sort by score descending
                .limit(limit)
                .collect(Collectors.toList());

        log.info("Found {} similar products for product ID: {}", similarProducts.size(), productId);
        return similarProducts;
    }

    private SimilarProductResponse calculateSimilarityScore(Product targetProduct, Product candidateProduct) {
        double totalScore = 0.0;

        // Gender similarity
        totalScore += calculateGenderSimilarity(targetProduct, candidateProduct) * GENDER_WEIGHT;

        // Category similarity
        totalScore += calculateCategorySimilarity(targetProduct, candidateProduct) * CATEGORY_WEIGHT;

        // Brand similarity
        totalScore += calculateBrandSimilarity(targetProduct, candidateProduct) * BRAND_WEIGHT;

        // Metadata similarities
        if (targetProduct.getMetadata() != null && candidateProduct.getMetadata() != null) {
            totalScore += calculateMetadataFieldSimilarity(targetProduct.getMetadata(), 
                    candidateProduct.getMetadata(), "material") * MATERIAL_WEIGHT;
            
            totalScore += calculateMetadataFieldSimilarity(targetProduct.getMetadata(), 
                    candidateProduct.getMetadata(), "season") * SEASON_WEIGHT;
            
            totalScore += calculateMetadataFieldSimilarity(targetProduct.getMetadata(), 
                    candidateProduct.getMetadata(), "occasion") * OCCASION_WEIGHT;
            
            totalScore += calculateMetadataFieldSimilarity(targetProduct.getMetadata(), 
                    candidateProduct.getMetadata(), "fit") * FIT_WEIGHT;
        }

        // Ensure score is between 0 and 1
        totalScore = Math.min(1.0, Math.max(0.0, totalScore));

        return mapToSimilarProductResponse(candidateProduct, Math.round(totalScore * 100.0) / 100.0);
    }

    private double calculateGenderSimilarity(Product targetProduct, Product candidateProduct) {
        if (targetProduct.getGender() == candidateProduct.getGender()) {
            return 1.0;
        }
        
        // UNISEX products are somewhat similar to any gender
        if (targetProduct.getGender().name().equals("UNISEX") || 
            candidateProduct.getGender().name().equals("UNISEX")) {
            return 0.5;
        }
        
        return 0.0;
    }

    private double calculateCategorySimilarity(Product targetProduct, Product candidateProduct) {
        if (targetProduct.getCategoryId().equals(candidateProduct.getCategoryId())) {
            return 1.0;
        }

        // Check if they have the same parent category (partial similarity)
        if (targetProduct.getCategory() != null && candidateProduct.getCategory() != null) {
            if (targetProduct.getCategory().getParentId() != null && 
                candidateProduct.getCategory().getParentId() != null &&
                targetProduct.getCategory().getParentId().equals(candidateProduct.getCategory().getParentId())) {
                return 0.6; // Same parent category
            }
            
            // Check if one is parent of the other
            if ((targetProduct.getCategory().getParentId() != null && 
                 targetProduct.getCategory().getParentId().equals(candidateProduct.getCategoryId())) ||
                (candidateProduct.getCategory().getParentId() != null && 
                 candidateProduct.getCategory().getParentId().equals(targetProduct.getCategoryId()))) {
                return 0.4; // Parent-child relationship
            }
        }

        return 0.0;
    }

    private double calculateBrandSimilarity(Product targetProduct, Product candidateProduct) {
        if (targetProduct.getBrandId().equals(candidateProduct.getBrandId())) {
            return 1.0;
        }
        return 0.0;
    }

    private double calculateMetadataFieldSimilarity(Map<String, Object> targetMetadata, 
                                                   Map<String, Object> candidateMetadata, 
                                                   String fieldName) {
        Object targetValue = targetMetadata.get(fieldName);
        Object candidateValue = candidateMetadata.get(fieldName);

        if (targetValue == null || candidateValue == null) {
            return 0.0;
        }

        if (targetValue.toString().equalsIgnoreCase(candidateValue.toString())) {
            return 1.0;
        }

        // Special handling for season - some seasons are more similar than others
        if ("season".equals(fieldName)) {
            return calculateSeasonSimilarity(targetValue.toString(), candidateValue.toString());
        }

        // Special handling for occasion - some occasions are more similar than others
        if ("occasion".equals(fieldName)) {
            return calculateOccasionSimilarity(targetValue.toString(), candidateValue.toString());
        }

        return 0.0;
    }

    private double calculateSeasonSimilarity(String season1, String season2) {
        if (season1.equalsIgnoreCase(season2)) {
            return 1.0;
        }

        // ALL_SEASON is somewhat compatible with everything
        if ("ALL_SEASON".equalsIgnoreCase(season1) || "ALL_SEASON".equalsIgnoreCase(season2)) {
            return 0.3;
        }

        // Spring and Summer are somewhat similar (warmer weather)
        if (("SPRING".equalsIgnoreCase(season1) && "SUMMER".equalsIgnoreCase(season2)) ||
            ("SUMMER".equalsIgnoreCase(season1) && "SPRING".equalsIgnoreCase(season2))) {
            return 0.4;
        }

        // Fall and Winter are somewhat similar (cooler weather)
        if (("FALL".equalsIgnoreCase(season1) && "WINTER".equalsIgnoreCase(season2)) ||
            ("WINTER".equalsIgnoreCase(season1) && "FALL".equalsIgnoreCase(season2))) {
            return 0.4;
        }

        return 0.0;
    }

    private double calculateOccasionSimilarity(String occasion1, String occasion2) {
        if (occasion1.equalsIgnoreCase(occasion2)) {
            return 1.0;
        }

        // CASUAL is somewhat compatible with SPORT
        if (("CASUAL".equalsIgnoreCase(occasion1) && "SPORT".equalsIgnoreCase(occasion2)) ||
            ("SPORT".equalsIgnoreCase(occasion1) && "CASUAL".equalsIgnoreCase(occasion2))) {
            return 0.3;
        }

        // FORMAL and BUSINESS are somewhat similar
        if (("FORMAL".equalsIgnoreCase(occasion1) && "BUSINESS".equalsIgnoreCase(occasion2)) ||
            ("BUSINESS".equalsIgnoreCase(occasion1) && "FORMAL".equalsIgnoreCase(occasion2))) {
            return 0.5;
        }

        // PARTY and FORMAL have some overlap
        if (("PARTY".equalsIgnoreCase(occasion1) && "FORMAL".equalsIgnoreCase(occasion2)) ||
            ("FORMAL".equalsIgnoreCase(occasion1) && "PARTY".equalsIgnoreCase(occasion2))) {
            return 0.3;
        }

        return 0.0;
    }

    private SimilarProductResponse mapToSimilarProductResponse(Product product, Double similarityScore) {
        // Get product images (not variant-specific)
        List<String> productImageUrls = productImageRepository.findByProductIdAndVariantIdIsNull(product.getId())
                .stream().map(ProductImage::getImageUrl).collect(Collectors.toList());

        // Start with the base ProductResponse structure
        SimilarProductResponse response = SimilarProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .brandId(product.getBrandId())
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .category(product.getCategory() != null ? 
                    com.charbel.ecommerce.category.dto.CategoryResponse.fromEntity(product.getCategory()) : null)
                .gender(product.getGender())
                .status(product.getStatus())
                .metadata(product.getMetadata())
                .imageUrls(productImageUrls)
                .variants(product.getVariants() != null
                        ? product.getVariants().stream().map(com.charbel.ecommerce.product.dto.ProductVariantResponse::fromEntity)
                                .collect(Collectors.toList())
                        : null)
                .reviewCount(product.getReviewCount())
                .averageRating(product.getAverageRating())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .similarityScore(similarityScore)
                .build();

        return response;
    }
}