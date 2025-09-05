package com.charbel.ecommerce.auth.controller;

import com.charbel.ecommerce.auth.dto.AuthResponse;
import com.charbel.ecommerce.auth.dto.LoginRequest;
import com.charbel.ecommerce.auth.dto.RegisterRequest;
import com.charbel.ecommerce.auth.dto.RefreshTokenRequest;
import com.charbel.ecommerce.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		log.info("Registration request received for email: {}", request.getEmail());
		AuthResponse response = authService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		log.info("Login request received for email: {}", request.getEmail());
		AuthResponse response = authService.login(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
		log.info("Token refresh request received");
		AuthResponse response = authService.refreshToken(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
		log.info("Logout request received");
		authService.logout(request.getRefreshToken());
		return ResponseEntity.ok().build();
	}
}
