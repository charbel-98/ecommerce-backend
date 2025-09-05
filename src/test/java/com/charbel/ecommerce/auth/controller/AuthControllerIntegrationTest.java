package com.charbel.ecommerce.auth.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.charbel.ecommerce.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

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

	@Autowired
	private UserRepository userRepository;

	@Test
	@DisplayName("Should register new user successfully")
	void shouldRegisterNewUserSuccessfully() throws Exception {
		// Given
		String registerRequest = """
				{
				    "email": "test@example.com",
				    "password": "password123",
				    "firstName": "John",
				    "lastName": "Doe"
				}
				""";

		// When & Then
		mockMvc.perform(
				post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerRequest).with(csrf()))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.userId").exists())
				.andExpect(jsonPath("$.email").value("test@example.com"))
				.andExpect(jsonPath("$.firstName").value("John")).andExpect(jsonPath("$.lastName").value("Doe"))
				.andExpect(jsonPath("$.role").value("CUSTOMER")).andExpect(jsonPath("$.accessToken").exists())
				.andExpect(jsonPath("$.refreshToken").exists()).andExpect(jsonPath("$.accessTokenExpiresAt").exists())
				.andExpect(jsonPath("$.refreshTokenExpiresAt").exists());
	}

	@Test
	@DisplayName("Should return conflict when registering existing user")
	void shouldReturnConflictWhenRegisteringExistingUser() throws Exception {
		// Given - First registration
		String registerRequest = """
				{
				    "email": "existing@example.com",
				    "password": "password123",
				    "firstName": "John",
				    "lastName": "Doe"
				}
				""";

		mockMvc.perform(
				post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerRequest).with(csrf()))
				.andExpect(status().isCreated());

		// When & Then - Second registration with same email
		mockMvc.perform(
				post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerRequest).with(csrf()))
				.andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.error").value("User Already Exists"))
				.andExpect(jsonPath("$.message").value("User with email existing@example.com already exists"));
	}

	@Test
	@DisplayName("Should return validation errors for invalid registration request")
	void shouldReturnValidationErrorsForInvalidRegistrationRequest() throws Exception {
		// Given
		String invalidRegisterRequest = """
				{
				    "email": "invalid-email",
				    "password": "123",
				    "firstName": "",
				    "lastName": ""
				}
				""";

		// When & Then
		mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(invalidRegisterRequest)
				.with(csrf())).andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Validation Failed"))
				.andExpect(jsonPath("$.validationErrors").exists());
	}

	@Test
	@DisplayName("Should login user successfully")
	void shouldLoginUserSuccessfully() throws Exception {
		// Given - Register user first
		String registerRequest = """
				{
				    "email": "login@example.com",
				    "password": "password123",
				    "firstName": "Jane",
				    "lastName": "Doe"
				}
				""";

		mockMvc.perform(
				post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerRequest).with(csrf()))
				.andExpect(status().isCreated());

		String loginRequest = """
				{
				    "email": "login@example.com",
				    "password": "password123"
				}
				""";

		// When & Then
		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginRequest).with(csrf()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.userId").exists())
				.andExpect(jsonPath("$.email").value("login@example.com"))
				.andExpect(jsonPath("$.firstName").value("Jane")).andExpect(jsonPath("$.lastName").value("Doe"))
				.andExpect(jsonPath("$.role").value("CUSTOMER")).andExpect(jsonPath("$.accessToken").exists())
				.andExpect(jsonPath("$.refreshToken").exists());
	}

	@Test
	@DisplayName("Should return unauthorized for invalid login credentials")
	void shouldReturnUnauthorizedForInvalidLoginCredentials() throws Exception {
		// Given
		String loginRequest = """
				{
				    "email": "nonexistent@example.com",
				    "password": "wrongpassword"
				}
				""";

		// When & Then
		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginRequest).with(csrf()))
				.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.error").value("Invalid Credentials"))
				.andExpect(jsonPath("$.message").value("Invalid email or password"));
	}

	@Test
	@DisplayName("Should refresh token successfully")
	void shouldRefreshTokenSuccessfully() throws Exception {
		// Given - Register and get initial tokens
		String registerRequest = """
				{
				    "email": "refresh@example.com",
				    "password": "password123",
				    "firstName": "Test",
				    "lastName": "User"
				}
				""";

		String registerResponse = mockMvc.perform(
				post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerRequest).with(csrf()))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

		var authResponse = objectMapper.readTree(registerResponse);
		String refreshToken = authResponse.get("refreshToken").asText();

		String refreshRequest = String.format("""
				{
				    "refreshToken": "%s"
				}
				""", refreshToken);

		// When & Then
		mockMvc.perform(
				post("/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(refreshRequest).with(csrf()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.userId").exists())
				.andExpect(jsonPath("$.email").value("refresh@example.com"))
				.andExpect(jsonPath("$.accessToken").exists()).andExpect(jsonPath("$.refreshToken").exists())
				.andExpect(jsonPath("$.accessToken").isNotEmpty()).andExpect(jsonPath("$.refreshToken").isNotEmpty());
	}

	@Test
	@DisplayName("Should return unauthorized for invalid refresh token")
	void shouldReturnUnauthorizedForInvalidRefreshToken() throws Exception {
		// Given
		String refreshRequest = """
				{
				    "refreshToken": "invalid-refresh-token"
				}
				""";

		// When & Then
		mockMvc.perform(
				post("/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(refreshRequest).with(csrf()))
				.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.error").value("Invalid Token"))
				.andExpect(jsonPath("$.message").value("Invalid refresh token"));
	}

	@Test
	@DisplayName("Should logout user successfully")
	void shouldLogoutUserSuccessfully() throws Exception {
		// Given - Register and get refresh token
		String registerRequest = """
				{
				    "email": "logout@example.com",
				    "password": "password123",
				    "firstName": "Test",
				    "lastName": "User"
				}
				""";

		String registerResponse = mockMvc.perform(
				post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerRequest).with(csrf()))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

		var authResponse = objectMapper.readTree(registerResponse);
		String refreshToken = authResponse.get("refreshToken").asText();

		String logoutRequest = String.format("""
				{
				    "refreshToken": "%s"
				}
				""", refreshToken);

		// When & Then
		mockMvc.perform(
				post("/auth/logout").contentType(MediaType.APPLICATION_JSON).content(logoutRequest).with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("Should return validation error for missing fields")
	void shouldReturnValidationErrorForMissingFields() throws Exception {
		// Given
		String emptyRequest = "{}";

		// When & Then
		mockMvc.perform(
				post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(emptyRequest).with(csrf()))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Validation Failed"));
	}
}
