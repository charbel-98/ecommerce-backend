package com.charbel.ecommerce.orders.entity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.charbel.ecommerce.address.entity.Address;
import com.charbel.ecommerce.common.entity.BaseEntity;
import com.charbel.ecommerce.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "order_number", nullable = true, unique = true, length = 20)
	private String orderNumber;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "address_id", nullable = false)
	private Address address;

	@Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalAmount;

	@Column(name = "original_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal originalAmount;

	@Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
	@Builder.Default
	private BigDecimal discountAmount = BigDecimal.ZERO;

	@Column(name = "delivery_fee", nullable = true, precision = 10, scale = 2)
	@Builder.Default
	private BigDecimal deliveryFee = new BigDecimal("5.00");

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private OrderStatus status = OrderStatus.PENDING;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<OrderItem> orderItems;

	public enum OrderStatus {
		PENDING, SHIPPED, COMPLETED, CANCELLED
	}
}
