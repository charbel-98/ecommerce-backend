package com.charbel.ecommerce.event.entity;

import com.charbel.ecommerce.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"discounts", "products"})
public class Event {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "image_url", nullable = false)
	private String imageUrl;

	@Column(name = "start_date", nullable = false)
	private LocalDateTime startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDateTime endDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private EventStatus status = EventStatus.ACTIVE;

	@OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Discount> discounts;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "event_products", joinColumns = @JoinColumn(name = "event_id"), inverseJoinColumns = @JoinColumn(name = "product_id"))
	private Set<Product> products;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public enum EventStatus {
		ACTIVE, INACTIVE, SCHEDULED, EXPIRED
	}

	public boolean isActive() {
		return status == EventStatus.ACTIVE && LocalDateTime.now().isAfter(startDate)
				&& LocalDateTime.now().isBefore(endDate);
	}
}