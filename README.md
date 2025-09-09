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

2. **Create and configure the `.env` file** (see Environment Variables section below)

3. **Start all services with Docker Compose**
   ```bash
   # Start all services (PostgreSQL + Spring Boot app)
   docker-compose up -d
   
   # Or start with logs visible
   docker-compose up
   ```

4. **Create the database (if not automatically created)**
   ```bash
   # Connect to PostgreSQL container and create database
   docker exec -it ecommerce_postgres psql -U ecommerce_user -c "CREATE DATABASE ecommerce_db;"
   
   # Or connect interactively to manage the database
   docker exec -it ecommerce_postgres psql -U ecommerce_user -d ecommerce_db
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

## Important Configuration Notes

### Spring Profiles
The application uses different Spring profiles for different environments:
- **`docker`**: For running with Docker Compose (uses `postgres:5432` hostname)
- **`dev`**: For local development (uses `localhost:5432` hostname)  
- **`prod`**: For production deployment

**⚠️ Critical**: When running with Docker, ensure `SPRING_PROFILES_ACTIVE=docker` in your `.env` file. Using `dev` profile with Docker will cause connection failures.

### Database Setup
The PostgreSQL container automatically creates the database and user based on environment variables. However, if you encounter issues:

1. **Verify the database exists:**
   ```bash
   docker exec -it ecommerce_postgres psql -U ecommerce_user -l
   ```

2. **Create database manually if needed:**
   ```bash
   docker exec -it ecommerce_postgres psql -U ecommerce_user -c "CREATE DATABASE ecommerce_db;"
   ```

3. **Test database connection:**
   ```bash
   docker exec -it ecommerce_postgres psql -U ecommerce_user -d ecommerce_db -c "SELECT 'Database connection successful!' as status;"
   ```

## Environment Variables

The application requires the following environment variables. Create a `.env` file in the project root:

```env
# DATABASE CONFIG
DATABASE_NAME=ecommerce_db
DATABASE_USERNAME=ecommerce_user
DATABASE_PASSWORD=ecommerce_password
DATABASE_PORT=5432

# JWT CONFIG
JWT_SECRET=ck
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# SPRING CONFIG
SPRING_PROFILES_ACTIVE=docker

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

### Database Connection Issues
```bash
# Check PostgreSQL container logs
docker-compose logs postgres

# Verify database exists and connection works
docker exec -it ecommerce_postgres psql -U ecommerce_user -d ecommerce_db -c "SELECT version();"

# Check if app can resolve postgres hostname
docker exec -it ecommerce_backend ping postgres

# Access PostgreSQL directly for debugging
docker-compose exec postgres psql -U ecommerce_user -d ecommerce_db

# Reset database (WARNING: Deletes all data)
docker-compose down -v
docker-compose up -d
```

### Spring Profile Issues
If you see connection errors like "Connection to localhost:5432 refused":
```bash
# Check the current Spring profile in container
docker exec ecommerce_backend env | grep SPRING_PROFILES_ACTIVE

# Ensure .env file has correct profile
echo "SPRING_PROFILES_ACTIVE=docker" >> .env

# Restart the application container
docker-compose restart app
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