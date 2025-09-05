package com.charbel.ecommerce.product.entity;

import com.charbel.ecommerce.brand.entity.Brand;
import com.charbel.ecommerce.category.entity.Category;
import com.charbel.ecommerce.common.enums.GenderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "base_price", nullable = false)
	private Integer basePrice;

	@Column(name = "brand_id", nullable = false)
	private UUID brandId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "brand_id", insertable = false, updatable = false)
	private Brand brand;

	@Column(name = "category_id", nullable = false)
	private UUID categoryId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", insertable = false, updatable = false)
	private Category category;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private GenderType gender;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private ProductStatus status = ProductStatus.ACTIVE;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private Map<String, Object> metadata;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ProductVariant> variants;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public enum ProductStatus {
		ACTIVE, INACTIVE
	}
}