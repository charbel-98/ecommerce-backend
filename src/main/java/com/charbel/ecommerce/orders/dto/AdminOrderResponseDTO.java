package com.charbel.ecommerce.orders.dto;

import com.charbel.ecommerce.orders.entity.Order.OrderStatus;
import com.charbel.ecommerce.user.entity.User.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOrderResponseDTO {

    private UUID id;
    private String orderNumber;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<OrderItemResponse> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserInfo user;
    private AddressInfo address;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private UUID id;
        private String email;
        private String firstName;
        private String lastName;
        private UserRole role;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressInfo {
        private UUID id;
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country;
        private Boolean isDefault;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}