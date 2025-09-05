package com.charbel.ecommerce.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

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

import com.charbel.ecommerce.user.entity.User;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

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
	private UserRepository userRepository;

	private User testUser;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();

		testUser = User.builder().email("test@example.com").passwordHash("hashedPassword").firstName("John")
				.lastName("Doe").role(User.UserRole.CUSTOMER).build();
	}

	@Test
	@DisplayName("Should save and retrieve user")
	void shouldSaveAndRetrieveUser() {
		// When
		User savedUser = userRepository.save(testUser);

		// Then
		assertThat(savedUser.getId()).isNotNull();
		assertThat(savedUser.getEmail()).isEqualTo(testUser.getEmail());
		assertThat(savedUser.getFirstName()).isEqualTo(testUser.getFirstName());
		assertThat(savedUser.getLastName()).isEqualTo(testUser.getLastName());
		assertThat(savedUser.getRole()).isEqualTo(testUser.getRole());
		assertThat(savedUser.getCreatedAt()).isNotNull();
		assertThat(savedUser.getUpdatedAt()).isNotNull();
	}

	@Test
	@DisplayName("Should find user by email")
	void shouldFindUserByEmail() {
		// Given
		userRepository.save(testUser);

		// When
		Optional<User> found = userRepository.findByEmail(testUser.getEmail());

		// Then
		assertThat(found).isPresent();
		assertThat(found.get().getEmail()).isEqualTo(testUser.getEmail());
	}

	@Test
	@DisplayName("Should return empty when user not found by email")
	void shouldReturnEmptyWhenUserNotFoundByEmail() {
		// When
		Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

		// Then
		assertThat(found).isEmpty();
	}

	@Test
	@DisplayName("Should check if user exists by email")
	void shouldCheckIfUserExistsByEmail() {
		// Given
		userRepository.save(testUser);

		// When & Then
		assertThat(userRepository.existsByEmail(testUser.getEmail())).isTrue();
		assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
	}

	@Test
	@DisplayName("Should enforce unique email constraint")
	void shouldEnforceUniqueEmailConstraint() {
		// Given
		userRepository.save(testUser);

		User duplicateUser = User.builder().email(testUser.getEmail()).passwordHash("anotherHashedPassword")
				.firstName("Jane").lastName("Smith").role(User.UserRole.ADMIN).build();

		// When & Then
		org.junit.jupiter.api.Assertions.assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
				() -> {
					userRepository.save(duplicateUser);
					userRepository.flush(); // Force the constraint check
				});
	}

	@Test
	@DisplayName("Should create user with default role")
	void shouldCreateUserWithDefaultRole() {
		// Given
		User userWithoutRole = User.builder().email("norole@example.com").passwordHash("hashedPassword").firstName("No")
				.lastName("Role").build();

		// When
		User saved = userRepository.save(userWithoutRole);

		// Then
		assertThat(saved.getRole()).isEqualTo(User.UserRole.CUSTOMER);
	}

	@Test
	@DisplayName("Should create admin user")
	void shouldCreateAdminUser() {
		// Given
		User adminUser = User.builder().email("admin@example.com").passwordHash("hashedPassword").firstName("Admin")
				.lastName("User").role(User.UserRole.ADMIN).build();

		// When
		User saved = userRepository.save(adminUser);

		// Then
		assertThat(saved.getRole()).isEqualTo(User.UserRole.ADMIN);
	}

	@Test
	@DisplayName("Should update user information")
	void shouldUpdateUserInformation() {
		// Given
		User saved = userRepository.save(testUser);
		String newFirstName = "UpdatedFirstName";
		String newLastName = "UpdatedLastName";

		// When
		saved.setFirstName(newFirstName);
		saved.setLastName(newLastName);
		User updated = userRepository.save(saved);

		// Then
		assertThat(updated.getFirstName()).isEqualTo(newFirstName);
		assertThat(updated.getLastName()).isEqualTo(newLastName);
		assertThat(updated.getUpdatedAt()).isNotNull();
		assertThat(updated.getUpdatedAt()).isAfter(updated.getCreatedAt());
	}
}
