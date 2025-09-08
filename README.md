# E-commerce Backend

A Spring Boot-based e-commerce backend application with PostgreSQL database, JWT authentication, and Cloudflare R2 integration.

## Quick Start with Docker

### Prerequisites
- Docker and Docker Compose installed on your system
- `.env` file properly configured (see Environment Variables section)

### Starting the Application

1. **Clone the repository and navigate to the project directory**
   ```bash
   cd ecommerce-backend
   ```

2. **Start all services with Docker Compose**
   ```bash
   # Start all services (PostgreSQL + Spring Boot app)
   docker-compose up -d
   
   # Or start with logs visible
   docker-compose up
   ```

3. **Check service status**
   ```bash
   docker-compose ps
   ```

4. **View application logs**
   ```bash
   # View all services logs
   docker-compose logs -f
   
   # View only app logs
   docker-compose logs -f app
   
   # View only database logs
   docker-compose logs -f postgres
   ```

### Stopping the Application

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: This will delete all data)
docker-compose down -v
```

### Development with PgAdmin (Optional)

To start with PgAdmin for database management:

```bash
# Start with dev profile to include PgAdmin
docker-compose --profile dev up -d
```

PgAdmin will be available at: http://localhost:5050
- Email: admin@ecommerce.local
- Password: admin123

## Environment Variables

The application requires the following environment variables. Create a `.env` file in the project root:

```env
# DATABASE CONFIG
DATABASE_NAME=ecommerce_db
DATABASE_USERNAME=ecommerce_user
DATABASE_PASSWORD=ecommerce_password
DATABASE_PORT=5432

# JWT CONFIG
JWT_SECRET=ThisIsAVeryLongAndSecureJWTSecretKeyForHMACSHA256Algorithm
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# SPRING CONFIG
SPRING_PROFILES_ACTIVE=dev

# APP CONFIG
APP_PORT=8080

# LOGGING CONFIG
LOG_LEVEL=INFO
SECURITY_LOG_LEVEL=INFO
WEB_LOG_LEVEL=INFO

# DATA SEEDING
SEED_DATA=true

# CLOUDFLARE CDN CONFIG
R2_BUCKET_NAME=your-bucket-name
R2_CDN_DOMAIN=your-cdn-domain
R2_ACCOUNT_ID=your-account-id
R2_ACCESS_KEY_ID=your-access-key-id
R2_SECRET_ACCESS_KEY=your-secret-access-key

# GOOGLE GEMINI AI CONFIG
GEMINI_API_KEY=your-gemini-api-key
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image-preview:generateContent
GEMINI_MAX_RETRIES=3
GEMINI_TIMEOUT=60000

# PGADMIN CONFIG (Optional - for development)
PGADMIN_EMAIL=admin@ecommerce.local
PGADMIN_PASSWORD=admin123
PGADMIN_PORT=5050
```

## API Endpoints

Once the application is running, it will be available at: http://localhost:8080

### Health Check
- `GET /actuator/health` - Application health status

### Authentication
- `POST /auth/register` - User registration
- `POST /auth/login` - User login

### Products
- `GET /products` - List all products
- `GET /products/{id}` - Get product details
- `POST /products` - Create product (Admin only)

### Orders
- `POST /orders` - Place an order (Authenticated users)
- `GET /orders/me` - Get current user's orders
- `GET /admin/orders` - Get all orders (Admin only)
- `GET /admin/low-stock` - Get low stock products (Admin only)

## Docker Services

### PostgreSQL Database
- **Container**: `ecommerce_postgres`
- **Port**: 5432
- **Database**: `ecommerce_db`
- **User**: `ecommerce_user`
- **Health Check**: Automatic with retry logic

### Spring Boot Application
- **Container**: `ecommerce_backend`
- **Port**: 8080
- **Profile**: `docker`
- **Health Check**: `/actuator/health` endpoint

### PgAdmin (Development)
- **Container**: `ecommerce_pgadmin`
- **Port**: 5050
- **Profile**: `dev` (optional)

## Troubleshooting

### Container Issues
```bash
# Check container status
docker-compose ps

# View container logs
docker-compose logs <service-name>

# Restart a specific service
docker-compose restart <service-name>

# Rebuild and restart
docker-compose up --build -d
```

### Database Issues
```bash
# Check PostgreSQL container logs
docker-compose logs postgres

# Access PostgreSQL directly
docker-compose exec postgres psql -U ecommerce_user -d ecommerce_db

# Reset database (WARNING: Deletes all data)
docker-compose down -v
docker-compose up -d
```

### Application Issues
```bash
# Check application logs
docker-compose logs app

# Check if application is responding
curl http://localhost:8080/actuator/health

# Restart only the application
docker-compose restart app
```

## Development

### Local Development (Non-Docker)

If you prefer to run the application locally for development:

1. Start only PostgreSQL with Docker:
   ```bash
   docker-compose up -d postgres
   ```

2. Set `SPRING_PROFILES_ACTIVE=dev` in your environment

3. Run the Spring Boot application with your IDE or Maven:
   ```bash
   ./mvnw spring-boot:run
   ```

### Building the Application

```bash
# Build the application
./mvnw clean package

# Build Docker image
docker build -t ecommerce-backend .

# Or build with Docker Compose
docker-compose build
```

## Production Deployment

For production deployment:

1. Set `SPRING_PROFILES_ACTIVE=prod` in your environment
2. Use strong, unique values for JWT_SECRET and database credentials
3. Configure proper networking and security groups
4. Use external PostgreSQL service if preferred
5. Set up proper logging and monitoring