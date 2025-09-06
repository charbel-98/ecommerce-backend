package com.charbel.ecommerce.ai.service;

import java.util.List;
import java.util.Map;

public interface ColorVariantImageService {
    /**
     * Generates enhanced product images for all color variants.
     *
     * @param originalImageBytes the uploaded product image
     * @param productName the product name (e.g., "shirt", "polo", "jacket")
     * @param colorVariants list of colors to generate (e.g., ["red", "black", "blue"])
     * @return map of identifiers to uploaded CDN URLs 
     *         (e.g., {"enhancedOriginal": "https://cdn.../enhanced.jpg", "red": "https://cdn.../red.jpg"})
     */
    Map<String, String> generateColorVariantImages(byte[] originalImageBytes, String productName, List<String> colorVariants);
}