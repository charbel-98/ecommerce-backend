package com.charbel.ecommerce.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.charbel.ecommerce.user.entity.RefreshToken;
import com.charbel.ecommerce.user.entity.User;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("RefreshTokenRepository Tests")
class RefreshTokenRepositoryTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15").withDatabaseName("testdb")
			.withUsername("test").withPassword("test");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EntityManager entityManager;

	private User testUser;
	private RefreshToken testRefreshToken;

	@BeforeEach
	void setUp() {
		refreshTokenRepository.deleteAll();
		userRepository.deleteAll();

		testUser = User.builder().email("test@example.com").passwordHash("hashedPassword").firstName("John")
				.lastName("Doe").role(User.UserRole.CUSTOMER).build();
		testUser = userRepository.save(testUser);

		testRefreshToken = RefreshToken.builder().token("testRefreshToken").user(testUser)
				.expiresAt(LocalDateTime.now().plusDays(7)).build();
	}

	@Test
	@DisplayName("Should save and retrieve refresh token")
	void shouldSaveAndRetrieveRefreshToken() {
		// When
		RefreshToken saved = refreshTokenRepository.save(testRefreshToken);

		// Then
		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getToken()).isEqualTo(testRefreshToken.getToken());
		assertThat(saved.getUser()).isEqualTo(testUser);
		assertThat(saved.getExpiresAt()).isEqualTo(testRefreshToken.getExpiresAt());
		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(saved.getRevokedAt()).isNull();
	}

	@Test
	@DisplayName("Should find refresh token by token and not revoked")
	void shouldFindRefreshTokenByTokenAndNotRevoked() {
		// Given
		refreshTokenRepository.save(testRefreshToken);

		// When
		Optional<RefreshToken> found = refreshTokenRepository
				.findByTokenAndRevokedAtIsNull(testRefreshToken.getToken());

		// Then
		assertThat(found).isPresent();
		assertThat(found.get().getToken()).isEqualTo(testRefreshToken.getToken());
	}

	@Test
	@DisplayName("Should not find revoked refresh token")
	void shouldNotFindRevokedRefreshToken() {
		// Given
		testRefreshToken.setRevokedAt(LocalDateTime.now());
		refreshTokenRepository.save(testRefreshToken);

		// When
		Optional<RefreshToken> found = refreshTokenRepository
				.findByTokenAndRevokedAtIsNull(testRefreshToken.getToken());

		// Then
		assertThat(found).isEmpty();
	}

	@Test
	@DisplayName("Should find all non-revoked tokens for user")
	void shouldFindAllNonRevokedTokensForUser() {
		// Given
		RefreshToken token1 = RefreshToken.builder().token("token1").user(testUser)
				.expiresAt(LocalDateTime.now().plusDays(7)).build();
		RefreshToken token2 = RefreshToken.builder().token("token2").user(testUser)
				.expiresAt(LocalDateTime.now().plusDays(7)).build();
		RefreshToken revokedToken = RefreshToken.builder().token("revokedToken").user(testUser)
				.expiresAt(LocalDateTime.now().plusDays(7)).revokedAt(LocalDateTime.now()).build();

		refreshTokenRepository.save(token1);
		refreshTokenRepository.save(token2);
		refreshTokenRepository.save(revokedToken);

		// When
		List<RefreshToken> found = refreshTokenRepository.findByUserAndRevokedAtIsNull(testUser);

		// Then
		assertThat(found).hasSize(2);
		assertThat(found).extracting(RefreshToken::getToken).containsExactlyInAnyOrder("token1", "token2");
	}

	@Test
	@DisplayName("Should revoke all user tokens")
	void shouldRevokeAllUserTokens() {
		// Given
		RefreshToken token1 = RefreshToken.builder().token("token1").user(testUser)
				.expiresAt(LocalDateTime.now().plusDays(7)).build();
		RefreshToken token2 = RefreshToken.builder().token("token2").user(testUser)
				.expiresAt(LocalDateTime.now().plusDays(7)).build();

		refreshTokenRepository.save(token1);
		refreshTokenRepository.save(token2);

		LocalDateTime revokedAt = LocalDateTime.now();

		// When
		refreshTokenRepository.revokeAllUserTokens(testUser, revokedAt);
		entityManager.clear(); // Clear persistence context to reload entities

		// Then
		List<RefreshToken> allTokens = refreshTokenRepository.findAll();
		assertThat(allTokens).hasSize(2);
		assertThat(allTokens).allMatch(token -> token.getRevokedAt() != null);
		assertThat(allTokens).allMatch(token -> !token.getRevokedAt().isBefore(revokedAt));
	}

	@Test
	@DisplayName("Should revoke token by token string")
	void shouldRevokeTokenByTokenString() {
		// Given
		refreshTokenRepository.save(testRefreshToken);
		LocalDateTime revokedAt = LocalDateTime.now();

		// When
		refreshTokenRepository.revokeByToken(testRefreshToken.getToken(), revokedAt);
		entityManager.clear(); // Clear persistence context to reload entities

		// Then
		RefreshToken refreshed = refreshTokenRepository.findAll().get(0);
		assertThat(refreshed.getRevokedAt()).isNotNull();
		assertThat(refreshed.getRevokedAt()).isEqualTo(revokedAt);
	}

	@Test
	@DisplayName("Should delete expired tokens")
	void shouldDeleteExpiredTokens() {
		// Given
		RefreshToken validToken = RefreshToken.builder().token("validToken").user(testUser)
				.expiresAt(LocalDateTime.now().plusDays(1)).build();
		RefreshToken expiredToken = RefreshToken.builder().token("expiredToken").user(testUser)
				.expiresAt(LocalDateTime.now().minusDays(1)).build();

		refreshTokenRepository.save(validToken);
		refreshTokenRepository.save(expiredToken);

		// When
		refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());

		// Then
		List<RefreshToken> remaining = refreshTokenRepository.findAll();
		assertThat(remaining).hasSize(1);
		assertThat(remaining.get(0).getToken()).isEqualTo("validToken");
	}

	@Test
	@DisplayName("Should check if token is expired")
	void shouldCheckIfTokenIsExpired() {
		// Given
		RefreshToken expiredToken = RefreshToken.builder().token("expiredToken").user(testUser)
				.expiresAt(LocalDateTime.now().minusDays(1)).build();
		RefreshToken validToken = RefreshToken.builder().token("validToken").user(testUser)
				.expiresAt(LocalDateTime.now().plusDays(1)).build();

		// Then
		assertThat(expiredToken.isExpired()).isTrue();
		assertThat(validToken.isExpired()).isFalse();
	}

	@Test
	@DisplayName("Should check if token is revoked")
	void shouldCheckIfTokenIsRevoked() {
		// Given
		RefreshToken revokedToken = RefreshToken.builder().token("revokedToken").user(testUser)
				.expiresAt(LocalDateTime.now().plusDays(1)).revokedAt(LocalDateTime.now()).build();
		RefreshToken activeToken = RefreshToken.builder().token("activeToken").user(testUser)
				.expiresAt(LocalDateTime.now().plusDays(1)).build();

		// Then
		assertThat(revokedToken.isRevoked()).isTrue();
		assertThat(activeToken.isRevoked()).isFalse();
	}

	@Test
	@DisplayName("Should check if token is valid")
	void shouldCheckIfTokenIsValid() {
		// Given
		RefreshToken validToken = RefreshToken.builder().token("validToken").user(testUser)
				.expiresAt(LocalDateTime.now().plusDays(1)).build();
		RefreshToken expiredToken = RefreshToken.builder().token("expiredToken").user(testUser)
				.expiresAt(LocalDateTime.now().minusDays(1)).build();
		RefreshToken revokedToken = RefreshToken.builder().token("revokedToken").user(testUser)
				.expiresAt(LocalDateTime.now().plusDays(1)).revokedAt(LocalDateTime.now()).build();

		// Then
		assertThat(validToken.isValid()).isTrue();
		assertThat(expiredToken.isValid()).isFalse();
		assertThat(revokedToken.isValid()).isFalse();
	}

	@Test
	@DisplayName("Should enforce unique token constraint")
	void shouldEnforceUniqueTokenConstraint() {
		// Given
		refreshTokenRepository.save(testRefreshToken);

		RefreshToken duplicateToken = RefreshToken.builder().token(testRefreshToken.getToken()).user(testUser)
				.expiresAt(LocalDateTime.now().plusDays(7)).build();

		// When & Then
		org.junit.jupiter.api.Assertions.assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
				() -> {
					refreshTokenRepository.save(duplicateToken);
					refreshTokenRepository.flush(); // Force the constraint check
				});
	}
}
