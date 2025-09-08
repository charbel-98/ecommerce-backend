package com.charbel.ecommerce.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.charbel.ecommerce.auth.dto.AuthResponse;
import com.charbel.ecommerce.auth.dto.LoginRequest;
import com.charbel.ecommerce.auth.dto.RefreshTokenRequest;
import com.charbel.ecommerce.auth.dto.RegisterRequest;
import com.charbel.ecommerce.exception.InvalidTokenException;
import com.charbel.ecommerce.exception.UserAlreadyExistsException;
import com.charbel.ecommerce.security.JwtService;
import com.charbel.ecommerce.user.entity.RefreshToken;
import com.charbel.ecommerce.user.entity.User;
import com.charbel.ecommerce.user.repository.RefreshTokenRepository;
import com.charbel.ecommerce.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	@Mock
	private AuthenticationManager authenticationManager;

	@InjectMocks
	private AuthService authService;

	private User testUser;
	private RegisterRequest registerRequest;
	private LoginRequest loginRequest;
	private RefreshToken refreshToken;

	@BeforeEach
	void setUp() {
		testUser = User.builder().id(UUID.randomUUID()).email("test@example.com").passwordHash("hashedPassword")
				.firstName("John").lastName("Doe").role(User.UserRole.CUSTOMER).build();

		registerRequest = RegisterRequest.builder().email("test@example.com").password("password123").firstName("John")
				.lastName("Doe").build();

		loginRequest = LoginRequest.builder().email("test@example.com").password("password123").build();

		refreshToken = RefreshToken.builder().id(UUID.randomUUID()).token("refreshToken").user(testUser)
				.expiresAt(LocalDateTime.now().plusDays(7)).build();
	}

	@Test
	@DisplayName("Should register new user successfully")
	void shouldRegisterNewUserSuccessfully() {
		// Given
		when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
		when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
		when(userRepository.save(any(User.class))).thenReturn(testUser);
		when(jwtService.generateAccessToken(testUser)).thenReturn("accessToken");
		when(jwtService.generateRefreshToken(testUser)).thenReturn("refreshToken");
		when(jwtService.getAccessTokenExpiration()).thenReturn(LocalDateTime.now().plusHours(24));
		when(jwtService.getRefreshTokenExpiration()).thenReturn(LocalDateTime.now().plusDays(7));
		when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

		// When
		AuthResponse response = authService.register(registerRequest);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
		assertThat(response.getFirstName()).isEqualTo(testUser.getFirstName());
		assertThat(response.getLastName()).isEqualTo(testUser.getLastName());
		assertThat(response.getRole()).isEqualTo(testUser.getRole());
		assertThat(response.getAccessToken()).isEqualTo("accessToken");
		assertThat(response.getRefreshToken()).isEqualTo("refreshToken");

		verify(userRepository).existsByEmail(registerRequest.getEmail());
		verify(passwordEncoder).encode(registerRequest.getPassword());
		verify(userRepository).save(any(User.class));
		verify(refreshTokenRepository).save(any(RefreshToken.class));
	}

	@Test
	@DisplayName("Should throw exception when user already exists")
	void shouldThrowExceptionWhenUserAlreadyExists() {
		// Given
		when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

		// When & Then
		assertThatThrownBy(() -> authService.register(registerRequest)).isInstanceOf(UserAlreadyExistsException.class)
				.hasMessageContaining("User with email test@example.com already exists");

		verify(userRepository).existsByEmail(registerRequest.getEmail());
		verify(userRepository, never()).save(any(User.class));
		verify(passwordEncoder, never()).encode(anyString());
	}

	@Test
	@DisplayName("Should login user successfully")
	void shouldLoginUserSuccessfully() {
		// Given
		when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
		when(jwtService.generateAccessToken(testUser)).thenReturn("accessToken");
		when(jwtService.generateRefreshToken(testUser)).thenReturn("refreshToken");
		when(jwtService.getAccessTokenExpiration()).thenReturn(LocalDateTime.now().plusHours(24));
		when(jwtService.getRefreshTokenExpiration()).thenReturn(LocalDateTime.now().plusDays(7));
		when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

		// When
		AuthResponse response = authService.login(loginRequest);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
		assertThat(response.getAccessToken()).isEqualTo("accessToken");
		assertThat(response.getRefreshToken()).isEqualTo("refreshToken");

		verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
		verify(userRepository).findByEmail(loginRequest.getEmail());
		verify(refreshTokenRepository).save(any(RefreshToken.class));
	}

	@Test
	@DisplayName("Should throw exception when login credentials are invalid")
	void shouldThrowExceptionWhenLoginCredentialsInvalid() {
		// Given
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("Invalid credentials"));

		// When & Then
		assertThatThrownBy(() -> authService.login(loginRequest)).isInstanceOf(BadCredentialsException.class);

		verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
		verify(userRepository, never()).findByEmail(anyString());
	}

	@Test
	@DisplayName("Should refresh tokens successfully")
	void shouldRefreshTokensSuccessfully() {
		// Given
		RefreshTokenRequest request = RefreshTokenRequest.builder().refreshToken("validRefreshToken").build();

		when(refreshTokenRepository.findByTokenAndRevokedAtIsNull("validRefreshToken"))
				.thenReturn(Optional.of(refreshToken));
		when(jwtService.generateAccessToken(testUser)).thenReturn("newAccessToken");
		when(jwtService.generateRefreshToken(testUser)).thenReturn("newRefreshToken");
		when(jwtService.getAccessTokenExpiration()).thenReturn(LocalDateTime.now().plusHours(24));
		when(jwtService.getRefreshTokenExpiration()).thenReturn(LocalDateTime.now().plusDays(7));
		when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

		// When
		AuthResponse response = authService.refreshToken(request);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
		assertThat(response.getRefreshToken()).isEqualTo("newRefreshToken");

		verify(refreshTokenRepository).findByTokenAndRevokedAtIsNull("validRefreshToken");
		verify(refreshTokenRepository).revokeByToken(anyString(), any(LocalDateTime.class));
		verify(refreshTokenRepository).save(any(RefreshToken.class));
	}

	@Test
	@DisplayName("Should throw exception when refresh token is invalid")
	void shouldThrowExceptionWhenRefreshTokenInvalid() {
		// Given
		RefreshTokenRequest request = RefreshTokenRequest.builder().refreshToken("invalidRefreshToken").build();

		when(refreshTokenRepository.findByTokenAndRevokedAtIsNull("invalidRefreshToken")).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> authService.refreshToken(request)).isInstanceOf(InvalidTokenException.class)
				.hasMessageContaining("Invalid refresh token");

		verify(refreshTokenRepository).findByTokenAndRevokedAtIsNull("invalidRefreshToken");
		verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
	}

	@Test
	@DisplayName("Should throw exception when refresh token is expired")
	void shouldThrowExceptionWhenRefreshTokenExpired() {
		// Given
		RefreshTokenRequest request = RefreshTokenRequest.builder().refreshToken("expiredRefreshToken").build();
		RefreshToken expiredToken = RefreshToken.builder().id(UUID.randomUUID()).token("expiredRefreshToken")
				.user(testUser).expiresAt(LocalDateTime.now().minusDays(1)) // Expired
				.build();

		when(refreshTokenRepository.findByTokenAndRevokedAtIsNull("expiredRefreshToken"))
				.thenReturn(Optional.of(expiredToken));

		// When & Then
		assertThatThrownBy(() -> authService.refreshToken(request)).isInstanceOf(InvalidTokenException.class)
				.hasMessageContaining("Refresh token is expired or revoked");

		verify(refreshTokenRepository).findByTokenAndRevokedAtIsNull("expiredRefreshToken");
		verify(refreshTokenRepository).revokeByToken(eq("expiredRefreshToken"), any(LocalDateTime.class));
	}

	@Test
	@DisplayName("Should logout user successfully")
	void shouldLogoutUserSuccessfully() {
		// Given
		String refreshToken = "validRefreshToken";

		// When
		authService.logout(refreshToken);

		// Then
		verify(refreshTokenRepository).revokeByToken(eq(refreshToken), any(LocalDateTime.class));
	}

	@Test
	@DisplayName("Should logout from all devices successfully")
	void shouldLogoutFromAllDevicesSuccessfully() {
		// Given & When
		authService.logoutAllDevices(testUser);

		// Then
		verify(refreshTokenRepository).revokeAllUserTokens(eq(testUser), any(LocalDateTime.class));
	}
}
