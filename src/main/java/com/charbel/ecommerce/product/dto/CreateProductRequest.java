package com.charbel.ecommerce.product.dto;

import com.charbel.ecommerce.common.enums.GenderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class CreateProductRequest {

	@NotBlank(message = "Product name is required")
	private String name;

	private String description;

	@NotNull(message = "Base price is required")
	@DecimalMin(value = "0.0", message = "Base price must be zero or positive")
	private BigDecimal basePrice;

	@NotNull(message = "Brand ID is required")
	private UUID brandId;

	@NotNull(message = "Category ID is required")
	private UUID categoryId;

	@NotNull(message = "Gender is required")
	private GenderType gender;

	private Map<String, Object> metadata;

	@NotEmpty(message = "At least one variant is required")
	@Valid
	private List<ProductVariantRequest> variants;

	@NotNull(message = "Product image is required")
	private MultipartFile image;
}
