package com.charbel.ecommerce.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
@DisplayName("Security Integration Tests")
class SecurityIntegrationTest {

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
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("Should allow access to public auth endpoints")
	void shouldAllowAccessToPublicAuthEndpoints() throws Exception {
		// Test registration endpoint
		String registerRequest = """
				{
				    "email": "public@example.com",
				    "password": "password123",
				    "firstName": "Public",
				    "lastName": "User"
				}
				""";

		mockMvc.perform(
				post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerRequest).with(csrf()))
				.andExpect(status().isCreated());

		// Test login endpoint
		String loginRequest = """
				{
				    "email": "public@example.com",
				    "password": "password123"
				}
				""";

		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginRequest).with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("Should deny access to admin endpoints without authentication")
	void shouldDenyAccessToAdminEndpointsWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/admin/test")).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401)).andExpect(jsonPath("$.error").value("Unauthorized"));
	}

	@Test
	@DisplayName("Should deny access to admin endpoints with customer role")
	@WithMockUser(roles = "CUSTOMER")
	void shouldDenyAccessToAdminEndpointsWithCustomerRole() throws Exception {
		mockMvc.perform(get("/admin/test")).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Should allow access to admin endpoints with admin role")
	@WithMockUser(roles = "ADMIN")
	void shouldAllowAccessToAdminEndpointsWithAdminRole() throws Exception {
		// This test assumes there would be admin endpoints that return 404 if they
		// don't exist
		// Since we don't have actual admin endpoints implemented, we expect either 404
		// or success
		mockMvc.perform(get("/admin/test")).andExpect(result -> {
			int status = result.getResponse().getStatus();
			// Admin role should not get 401 or 403
			assert (status != 401 && status != 403);
		});
	}

	@Test
	@DisplayName("Should authenticate with valid JWT token")
	void shouldAuthenticateWithValidJWTToken() throws Exception {
		// First, register and get a token
		String registerRequest = """
				{
				    "email": "jwt@example.com",
				    "password": "password123",
				    "firstName": "JWT",
				    "lastName": "User"
				}
				""";

		String response = mockMvc.perform(
				post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerRequest).with(csrf()))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

		var authResponse = objectMapper.readTree(response);
		String accessToken = authResponse.get("accessToken").asText();

		// Test access with JWT token - try to access a protected endpoint
		// Since we don't have non-admin protected endpoints, we'll use admin endpoint
		// and expect 403 (forbidden) instead of 401 (unauthorized)
		mockMvc.perform(get("/admin/test").header("Authorization", "Bearer " + accessToken)).andExpect(result -> {
			int status = result.getResponse().getStatus();
			// With valid token, should get 403 (forbidden) not 401 (unauthorized)
			// because user has CUSTOMER role trying to access admin endpoint
			assert (status == 403 || status == 404); // 404 if endpoint doesn't exist
			assert (status != 401); // Should not be unauthorized with valid token
		});
	}

	@Test
	@DisplayName("Should reject invalid JWT token")
	void shouldRejectInvalidJWTToken() throws Exception {
		mockMvc.perform(get("/admin/test").header("Authorization", "Bearer invalid-token"))
				.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
	}

	@Test
	@DisplayName("Should reject expired JWT token")
	void shouldRejectExpiredJWTToken() throws Exception {
		// This is a manually crafted expired token for testing purposes
		String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjAwMDAwMDAwLCJleHAiOjE2MDAwMDAwMDB9.invalid";

		mockMvc.perform(get("/admin/test").header("Authorization", "Bearer " + expiredToken))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Should handle missing Authorization header")
	void shouldHandleMissingAuthorizationHeader() throws Exception {
		mockMvc.perform(get("/admin/test")).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401));
	}

	@Test
	@DisplayName("Should handle malformed Authorization header")
	void shouldHandleMalformedAuthorizationHeader() throws Exception {
		mockMvc.perform(get("/admin/test").header("Authorization", "InvalidHeaderFormat"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Should allow CSRF for auth endpoints")
	void shouldAllowCSRFForAuthEndpoints() throws Exception {
		String registerRequest = """
				{
				    "email": "csrf@example.com",
				    "password": "password123",
				    "firstName": "CSRF",
				    "lastName": "Test"
				}
				""";

		// Test with CSRF token
		mockMvc.perform(
				post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerRequest).with(csrf()))
				.andExpect(status().isCreated());
	}
}
