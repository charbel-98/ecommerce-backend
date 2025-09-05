package com.charbel.ecommerce.auth.service;

import com.charbel.ecommerce.auth.dto.AuthResponse;
import com.charbel.ecommerce.auth.dto.LoginRequest;
import com.charbel.ecommerce.auth.dto.RegisterRequest;
import com.charbel.ecommerce.auth.dto.RefreshTokenRequest;
import com.charbel.ecommerce.exception.InvalidTokenException;
import com.charbel.ecommerce.exception.UserAlreadyExistsException;
import com.charbel.ecommerce.security.JwtService;
import com.charbel.ecommerce.user.entity.RefreshToken;
import com.charbel.ecommerce.user.entity.User;
import com.charbel.ecommerce.user.repository.RefreshTokenRepository;
import com.charbel.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	public AuthResponse register(RegisterRequest request) {
		log.info("Registering new user with email: {}", request.getEmail());

		if (userRepository.existsByEmail(request.getEmail())) {
			throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
		}

		User user = User.builder().email(request.getEmail()).passwordHash(passwordEncoder.encode(request.getPassword()))
				.firstName(request.getFirstName()).lastName(request.getLastName()).role(User.UserRole.CUSTOMER).build();

		user = userRepository.save(user);
		log.info("User registered successfully with ID: {}", user.getId());

		return generateAuthResponse(user);
	}

	public AuthResponse login(LoginRequest request) {
		log.info("User login attempt for email: {}", request.getEmail());

		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new InvalidTokenException("Invalid credentials"));

		log.info("User logged in successfully: {}", user.getId());
		return generateAuthResponse(user);
	}

	public AuthResponse refreshToken(RefreshTokenRequest request) {
		log.info("Refresh token request received");

		RefreshToken refreshToken = refreshTokenRepository.findByTokenAndRevokedAtIsNull(request.getRefreshToken())
				.orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

		if (!refreshToken.isValid()) {
			log.warn("Invalid or expired refresh token for user: {}", refreshToken.getUser().getId());
			refreshTokenRepository.revokeByToken(request.getRefreshToken(), LocalDateTime.now());
			throw new InvalidTokenException("Refresh token is expired or revoked");
		}

		User user = refreshToken.getUser();
		log.info("Refreshing tokens for user: {}", user.getId());

		refreshTokenRepository.revokeByToken(request.getRefreshToken(), LocalDateTime.now());
		return generateAuthResponse(user);
	}

	public void logout(String refreshToken) {
		log.info("User logout request");
		refreshTokenRepository.revokeByToken(refreshToken, LocalDateTime.now());
		log.info("User logged out successfully");
	}

	public void logoutAllDevices(User user) {
		log.info("Logout all devices for user: {}", user.getId());
		refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now());
		log.info("All tokens revoked for user: {}", user.getId());
	}

	private AuthResponse generateAuthResponse(User user) {
		String accessToken = jwtService.generateAccessToken(user);
		String refreshToken = jwtService.generateRefreshToken(user);

		RefreshToken refreshTokenEntity = RefreshToken.builder().token(refreshToken).user(user)
				.expiresAt(jwtService.getRefreshTokenExpiration()).build();

		refreshTokenRepository.save(refreshTokenEntity);

		return AuthResponse.builder().userId(user.getId()).email(user.getEmail()).firstName(user.getFirstName())
				.lastName(user.getLastName()).role(user.getRole()).accessToken(accessToken).refreshToken(refreshToken)
				.accessTokenExpiresAt(jwtService.getAccessTokenExpiration())
				.refreshTokenExpiresAt(jwtService.getRefreshTokenExpiration()).build();
	}
}
