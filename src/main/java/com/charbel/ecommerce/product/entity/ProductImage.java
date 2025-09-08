package com.charbel.ecommerce.product.entity;

import com.charbel.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "product_images")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "product_id", nullable = false)
	private UUID productId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", insertable = false, updatable = false)
	private Product product;

	@Column(name = "variant_id")
	private UUID variantId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "variant_id", insertable = false, updatable = false)
	private ProductVariant variant;

	@Column(name = "image_url", nullable = false)
	private String imageUrl;

	@Column(name = "alt_text")
	private String altText;

	@Column(name = "is_primary", nullable = false)
	@Builder.Default
	private Boolean isPrimary = false;

	@Column(name = "sort_order", nullable = false)
	@Builder.Default
	private Integer sortOrder = 0;

}
