Backend AI Rules (Spring Boot)

Project Setup: Use Java 17+ with Spring Boot. In your build file (Maven/Gradle), include typical starters: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security, spring-boot-starter-validation, and testing libraries. Follow Java package naming (e.g. com.example.myapp)
docs.spring.io
. Place the main application class (annotated @SpringBootApplication) in the root package to enable component scanning
docs.spring.io
.

Package Structure: Under the root package, create domain-based packages: controller, service, repository, model (entities), dto, and security/config. For example:

com.example.ecommerce
  ├─ Application.java (@SpringBootApplication) 
  ├─ auth 
  │   ├─ AuthController.java 
  │   ├─ AuthService.java
  │   └─ AuthRepository.java
  ├─ products 
  │   ├─ ProductController.java 
  │   ├─ ProductService.java 
  │   └─ ProductRepository.java
  ├─ orders 
  │   ├─ OrderController.java 
  │   ├─ OrderService.java 
  │   └─ OrderRepository.java
  └─ (others: model, dto, config, etc.)


This clear layering (Controller-Service-Repository) enforces separation of concerns
medium.com
docs.spring.io
. Use DTO classes for request/response models to decouple API from entities
medium.com
.

Entities and Models: Define JPA entities for User, Product, Order, and OrderItem. Include appropriate fields (e.g. Product has id, name, price, stock). Store monetary values as BigDecimal (to avoid floating errors). Annotate with @Entity and use plural table names. For example, a Product entity with fields (Long id, String name, BigDecimal price, int stock). Generate boilerplate with Lombok if allowed. Ensure proper @OneToMany / @ManyToOne relations (e.g. Order and OrderItem).

Repositories: Use Spring Data JPA interfaces (extends JpaRepository<...>). For example, UserRepository, ProductRepository, OrderRepository. These handle CRUD without boilerplate. Enable automatic schema generation or use Flyway/Liquibase migrations as needed.

Data Transfer Objects (DTOs): For each entity, create corresponding DTOs for incoming/outgoing data (e.g. UserDTO, ProductDTO, OrderDTO). Do not expose entities directly in controllers. Convert between entities and DTOs in the service layer or with a mapper (e.g. MapStruct or manual mapping)
medium.com
.

Authentication (Auth Endpoints): Implement /auth/register and /auth/login in an AuthController.

POST /auth/register: accepts JSON { "email", "password" }, validates inputs, hashes password (e.g. BCrypt), saves new user, and returns 201 with user info.

POST /auth/login: accepts credentials, authenticates via Spring Security, and returns 200 with a JWT token.
Use Spring Security filters to generate and validate JWT Bearer tokens on authenticated endpoints. Secure passwords and never log them. Store the JWT secret in configuration.

Controllers & Endpoints: Follow REST best practices with noun-based, plural resource names
restfulapi.net
restfulapi.net
. Key endpoints:

GET /products – list all products (200).

GET /products/{id} – get product details (200).

POST /products – admin-only create a product (201).

POST /orders – authenticated users place an order (201). Body: {"items":[{"productId", "quantity"}, ...]}. Implement order placement in a single transaction: check stock and decrement atomically, failing if insufficient.

GET /orders/me – list current user’s orders (200).

GET /admin/orders – admin-only list all orders (200).

GET /admin/low-stock – admin-only list products with stock < 5 (200).
Ensure to return correct HTTP status codes (201 for created resources, 200 for OK, 401 for unauthorized, 400 for bad requests, etc.). Use @RestController and annotations like @GetMapping and @PostMapping. Validate request bodies with @Valid and Spring’s validation annotations.

Service Layer: Implement business logic in @Service classes. For example, ProductService handles product operations, OrderService handles order creation (including stock checks)
medium.com
. Annotate the order placement method with @Transactional to guarantee atomicity of stock updates. Consider using pessimistic locking (@Lock) on Product.stock or custom SQL (e.g. WHERE stock>=?) to avoid race conditions when multiple orders occur.

Security Configuration: Use Spring Security with JWT. Configure an AuthenticationFilter to intercept Bearer tokens and populate SecurityContext. Define roles (ROLE_USER, ROLE_ADMIN). Protect endpoints so that admin routes (/admin/**, POST /products) require admin role. Public routes (/products GET, /auth/**) remain open. On any 401 response, the client should logout.

Error Handling: Add a @ControllerAdvice global exception handler to return consistent JSON errors. For example, if an order fails due to insufficient stock, throw a custom exception and return 400 Bad Request with a clear message. Handle validation errors to return 400 with field error details. Always fail gracefully: unexpected errors should return 500 Internal Server Error with minimal info.

Persistence (Database): Use Postgres in production (configure via application.properties or env vars). For development and testing, H2 in-memory is acceptable. Store configurations (DB URL, user, JWT secret) in environment or application.yml. Use connection pooling (HikariCP) out of the box.

ORM (Postgres): Use Hibernate (JPA provider) with Spring Data JPA as the ORM. Configure the PostgreSQL dialect (`spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect`). For JSON/JSONB columns, use the Hibernate Types library (`com.vladmihalcea:hibernate-types-60`) and map fields with `@Type(JsonType.class)`. Enable batching (`spring.jpa.properties.hibernate.jdbc.batch_size`) for better write performance.

Transactions & Concurrency: Ensure placing orders is transactionally safe: mark the order service method @Transactional so all stock updates commit or roll back together. Optionally, use JPA’s locking (e.g. @Lock(PESSIMISTIC_WRITE)) on Product to prevent overselling. Document any choice made.

Testing: Write unit tests for core logic (e.g. order placement, stock calculations). Use Spring’s testing framework (@SpringBootTest, MockMvc) to test controllers. Aim for coverage of critical paths: authentication flow, creating products, placing orders, and error cases.

Dependencies & Libraries: Include relevant Spring Boot starters (Web, JPA, Security, Validation, Test). Use Lombok for boilerplate reduction (optional). If using JWT, include a library (e.g. io.jsonwebtoken:jjwt or Spring’s OAuth2 JWT support). Ensure all libraries are up-to-date and null-safe.