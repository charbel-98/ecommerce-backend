package com.charbel.ecommerce.product.controller;

import com.charbel.ecommerce.product.entity.Product;
import com.charbel.ecommerce.product.entity.ProductVariant;
import com.charbel.ecommerce.product.repository.ProductRepository;
import com.charbel.ecommerce.product.repository.ProductVariantRepository;
import com.charbel.ecommerce.user.entity.User;
import com.charbel.ecommerce.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerAdminIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ProductVariantRepository productVariantRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private String adminToken;

	@BeforeEach
	void setUp() {
		User adminUser = User.builder().email("admin@test.com").passwordHash(passwordEncoder.encode("password"))
				.firstName("Admin").lastName("User").role(User.UserRole.ADMIN).build();

		userRepository.save(adminUser);

		// Create a low stock product variant for testing
		Product product = Product.builder().name("Test Product").description("Test Description").basePrice(2999)
				.status(Product.ProductStatus.ACTIVE).build();

		Product savedProduct = productRepository.save(product);

		ProductVariant lowStockVariant = ProductVariant.builder().product(savedProduct).sku("LOW-STOCK-001")
				.attributes(Map.of("size", "S", "color", "Blue")).price(2999).stock(3) // Low stock
				.build();

		productVariantRepository.save(lowStockVariant);

		adminToken = "Bearer mock-admin-token";
	}

	@Test
	void getLowStockProducts_ShouldReturnLowStockProducts() throws Exception {
		mockMvc.perform(get("/products/low-stock").header("Authorization", adminToken)).andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$[0].sku").value("LOW-STOCK-001"))
				.andExpect(jsonPath("$[0].stock").value(3))
				.andExpect(jsonPath("$[0].productName").value("Test Product"));
	}

	@Test
	void getLowStockProducts_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
		mockMvc.perform(get("/products/low-stock")).andExpect(status().isUnauthorized());
	}
}
