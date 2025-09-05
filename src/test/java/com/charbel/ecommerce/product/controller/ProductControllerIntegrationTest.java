package com.charbel.ecommerce.product.controller;

import com.charbel.ecommerce.auth.service.AuthService;
import com.charbel.ecommerce.product.dto.CreateProductRequest;
import com.charbel.ecommerce.product.dto.ProductVariantRequest;
import com.charbel.ecommerce.user.entity.User;
import com.charbel.ecommerce.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuthService authService;

	private String adminToken;

	@BeforeEach
	void setUp() {
		User adminUser = User.builder()
				.email("admin@test.com")
				.passwordHash(passwordEncoder.encode("password"))
				.firstName("Admin")
				.lastName("User")
				.role(User.UserRole.ADMIN)
				.build();
		
		userRepository.save(adminUser);
		
		// Get admin token (simplified for test)
		adminToken = "Bearer mock-admin-token";
	}

	@Test
	void createProduct_WithValidRequest_ShouldReturnCreated() throws Exception {
		ProductVariantRequest variantRequest = new ProductVariantRequest();
		variantRequest.setSku("TEST-001");
		variantRequest.setAttributes(Map.of("size", "M", "color", "Red"));
		variantRequest.setPrice(2999);
		variantRequest.setStock(10);

		CreateProductRequest request = new CreateProductRequest();
		request.setName("Test Product");
		request.setDescription("Test Description");
		request.setBasePrice(2999);
		request.setVariants(List.of(variantRequest));

		mockMvc.perform(post("/products")
				.header("Authorization", adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Test Product"))
				.andExpect(jsonPath("$.description").value("Test Description"))
				.andExpect(jsonPath("$.basePrice").value(2999))
				.andExpect(jsonPath("$.variants[0].sku").value("TEST-001"))
				.andExpect(jsonPath("$.variants[0].price").value(2999))
				.andExpect(jsonPath("$.variants[0].stock").value(10));
	}

	@Test
	void createProduct_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
		ProductVariantRequest variantRequest = new ProductVariantRequest();
		variantRequest.setSku("TEST-001");
		variantRequest.setAttributes(Map.of("size", "M"));
		variantRequest.setPrice(2999);
		variantRequest.setStock(10);

		CreateProductRequest request = new CreateProductRequest();
		request.setName("Test Product");
		request.setBasePrice(2999);
		request.setVariants(List.of(variantRequest));

		mockMvc.perform(post("/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void createProduct_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
		CreateProductRequest request = new CreateProductRequest();
		// Missing required fields

		mockMvc.perform(post("/products")
				.header("Authorization", adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}
}