package com.charbel.ecommerce.brand.entity;

import com.charbel.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "brands")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Brand extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, unique = true)
	private String name;

	@Column(nullable = false, unique = true)
	private String slug;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "logo_url")
	private String logoUrl;

	@Column(name = "website_url")
	private String websiteUrl;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private BrandStatus status = BrandStatus.ACTIVE;

	public enum BrandStatus {
		ACTIVE, INACTIVE
	}
}
