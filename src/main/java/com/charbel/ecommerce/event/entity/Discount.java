package com.charbel.ecommerce.event.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

	@Column(nullable = false)
	private Integer value; // percentage (0-100) or fixed amount in cents

	@Column(name = "min_purchase_amount")
	private Integer minPurchaseAmount; // minimum purchase required in cents

	@Column(name = "max_discount_amount")
	private Integer maxDiscountAmount; // max discount for percentage type in cents

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public enum DiscountType {
		PERCENTAGE, FIXED_AMOUNT
	}

	public int calculateDiscountAmount(int totalAmount) {
		if (minPurchaseAmount != null && totalAmount < minPurchaseAmount) {
			return 0;
		}

		int discountAmount;
		if (type == DiscountType.PERCENTAGE) {
			discountAmount = (totalAmount * value) / 100;
			if (maxDiscountAmount != null && discountAmount > maxDiscountAmount) {
				discountAmount = maxDiscountAmount;
			}
		} else {
			discountAmount = value;
		}

		return Math.min(discountAmount, totalAmount);
	}
}
