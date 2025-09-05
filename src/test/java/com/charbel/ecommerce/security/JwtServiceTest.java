package com.charbel.ecommerce.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@DisplayName("JwtService Tests")
class JwtServiceTest {

	private JwtService jwtService;
	private com.charbel.ecommerce.user.entity.User testUser;
	private final String secret = "ThisIsAVeryLongAndSecureJWTSecretKeyForHMACSHA256Algorithm";
	private final long jwtExpiration = 86400000L; // 24 hours
	private final long refreshExpiration = 604800000L; // 7 days

	@BeforeEach
	void setUp() {
		jwtService = new JwtService();
		ReflectionTestUtils.setField(jwtService, "secret", secret);
		ReflectionTestUtils.setField(jwtService, "jwtExpiration", jwtExpiration);
		ReflectionTestUtils.setField(jwtService, "refreshExpiration", refreshExpiration);

		testUser = com.charbel.ecommerce.user.entity.User.builder().id(UUID.randomUUID()).email("test@example.com")
				.firstName("John").lastName("Doe").role(com.charbel.ecommerce.user.entity.User.UserRole.CUSTOMER)
				.build();
	}

	@Test
	@DisplayName("Should generate access token with correct claims")
	void shouldGenerateAccessTokenWithCorrectClaims() {
		// When
		String token = jwtService.generateAccessToken(testUser);

		// Then
		assertThat(token).isNotNull();
		assertThat(token.split("\\.")).hasSize(3); // JWT should have 3 parts

		// Verify claims
		Claims claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secret.getBytes())).build()
				.parseClaimsJws(token).getBody();

		assertThat(claims.getSubject()).isEqualTo(testUser.getEmail());
		assertThat(claims.get("userId")).isEqualTo(testUser.getId().toString());
		assertThat(claims.get("role")).isEqualTo(testUser.getRole().name());
		assertThat(claims.get("firstName")).isEqualTo(testUser.getFirstName());
		assertThat(claims.get("lastName")).isEqualTo(testUser.getLastName());
		assertThat(claims.getIssuedAt()).isNotNull();
		assertThat(claims.getExpiration()).isNotNull();
		assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
	}

	@Test
	@DisplayName("Should generate refresh token with correct subject")
	void shouldGenerateRefreshTokenWithCorrectSubject() {
		// When
		String token = jwtService.generateRefreshToken(testUser);

		// Then
		assertThat(token).isNotNull();
		assertThat(token.split("\\.")).hasSize(3);

		// Verify claims
		Claims claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secret.getBytes())).build()
				.parseClaimsJws(token).getBody();

		assertThat(claims.getSubject()).isEqualTo(testUser.getEmail());
		assertThat(claims.getIssuedAt()).isNotNull();
		assertThat(claims.getExpiration()).isNotNull();
		assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
	}

	@Test
	@DisplayName("Should extract username from token")
	void shouldExtractUsernameFromToken() {
		// Given
		String token = jwtService.generateAccessToken(testUser);

		// When
		String username = jwtService.extractUsername(token);

		// Then
		assertThat(username).isEqualTo(testUser.getEmail());
	}

	@Test
	@DisplayName("Should extract expiration date from token")
	void shouldExtractExpirationDateFromToken() {
		// Given
		String token = jwtService.generateAccessToken(testUser);

		// When
		Date expiration = jwtService.extractExpiration(token);

		// Then
		assertThat(expiration).isNotNull();
		assertThat(expiration).isAfter(new Date());
	}

	@Test
	@DisplayName("Should validate token successfully for correct user")
	void shouldValidateTokenSuccessfullyForCorrectUser() {
		// Given
		String token = jwtService.generateAccessToken(testUser);
		UserDetails userDetails = User.withUsername(testUser.getEmail()).password("password").roles("USER").build();

		// When
		boolean isValid = jwtService.isTokenValid(token, userDetails);

		// Then
		assertThat(isValid).isTrue();
	}

	@Test
	@DisplayName("Should not validate token for different user")
	void shouldNotValidateTokenForDifferentUser() {
		// Given
		String token = jwtService.generateAccessToken(testUser);
		UserDetails userDetails = User.withUsername("different@example.com").password("password").roles("USER").build();

		// When
		boolean isValid = jwtService.isTokenValid(token, userDetails);

		// Then
		assertThat(isValid).isFalse();
	}

	@Test
	@DisplayName("Should detect token is not expired")
	void shouldDetectTokenIsNotExpired() {
		// Given
		String token = jwtService.generateAccessToken(testUser);

		// When
		boolean isExpired = jwtService.isTokenExpired(token);

		// Then
		assertThat(isExpired).isFalse();
	}

	@Test
	@DisplayName("Should detect expired token")
	void shouldDetectExpiredToken() {
		// Given - Generate token with very short expiration
		String token = jwtService.generateToken(java.util.Map.of(), testUser.getEmail(), 1L); // 1ms

		// Wait for token to expire
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		// When
		boolean isExpired = jwtService.isTokenExpired(token);

		// Then
		assertThat(isExpired).isTrue();
	}

	@Test
	@DisplayName("Should generate tokens with different expiration times")
	void shouldGenerateTokensWithDifferentExpirationTimes() {
		// When
		String accessToken = jwtService.generateAccessToken(testUser);
		String refreshToken = jwtService.generateRefreshToken(testUser);

		// Then
		Date accessTokenExpiration = jwtService.extractExpiration(accessToken);
		Date refreshTokenExpiration = jwtService.extractExpiration(refreshToken);

		assertThat(refreshTokenExpiration).isAfter(accessTokenExpiration);
	}

	@Test
	@DisplayName("Should get correct access token expiration time")
	void shouldGetCorrectAccessTokenExpirationTime() {
		// When
		var expiration = jwtService.getAccessTokenExpiration();

		// Then
		assertThat(expiration).isNotNull();
		assertThat(expiration).isAfter(java.time.LocalDateTime.now());
		assertThat(expiration).isBefore(java.time.LocalDateTime.now().plusDays(2)); // Should be within 24 hours
	}

	@Test
	@DisplayName("Should get correct refresh token expiration time")
	void shouldGetCorrectRefreshTokenExpirationTime() {
		// When
		var expiration = jwtService.getRefreshTokenExpiration();

		// Then
		assertThat(expiration).isNotNull();
		assertThat(expiration).isAfter(java.time.LocalDateTime.now());
		assertThat(expiration).isBefore(java.time.LocalDateTime.now().plusDays(8)); // Should be within 7 days
	}
}
