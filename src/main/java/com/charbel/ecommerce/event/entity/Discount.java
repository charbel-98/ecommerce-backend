package com.charbel.ecommerce.event.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "discounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Discount {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "event_id", nullable = false)
	private UUID eventId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "event_id", insertable = false, updatable = false)
	private Event event;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DiscountType type;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal value; // percentage (25.00 for 25%) for PERCENTAGE type, or amount in dollars for FIXED_AMOUNT type

	@Column(name = "min_purchase_amount", precision = 10, scale = 2)
	private BigDecimal minPurchaseAmount; // minimum purchase required in dollars

	@Column(name = "max_discount_amount", precision = 10, scale = 2)
	private BigDecimal maxDiscountAmount; // max discount for percentage type in dollars

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public enum DiscountType {
		PERCENTAGE, FIXED_AMOUNT
	}

	public BigDecimal calculateDiscountAmount(BigDecimal totalAmount) {
		if (minPurchaseAmount != null && totalAmount.compareTo(minPurchaseAmount) < 0) {
			return BigDecimal.ZERO;
		}

		BigDecimal discountAmount;
		if (type == DiscountType.PERCENTAGE) {
			// For percentage, value represents the percentage (e.g., 25.00 for 25%)
			discountAmount = totalAmount.multiply(value).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
			if (maxDiscountAmount != null && discountAmount.compareTo(maxDiscountAmount) > 0) {
				discountAmount = maxDiscountAmount;
			}
		} else {
			// For fixed amount, value is the discount amount in dollars
			discountAmount = value;
		}

		return discountAmount.min(totalAmount);
	}
}
