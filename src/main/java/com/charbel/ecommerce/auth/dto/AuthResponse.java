package com.charbel.ecommerce.auth.dto;

import com.charbel.ecommerce.user.entity.User.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

	private UUID userId;
	private String email;
	private String firstName;
	private String lastName;
	private UserRole role;
	private String accessToken;
	private String refreshToken;
	private LocalDateTime accessTokenExpiresAt;
	private LocalDateTime refreshTokenExpiresAt;
}
