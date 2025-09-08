# Docker Setup for E-commerce Backend

This guide will help you run the E-commerce Backend application seamlessly on any machine using Docker and Docker Compose.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) (version 20.10+)
- [Docker Compose](https://docs.docker.com/compose/install/) (version 2.0+)
- At least 4GB of available RAM
- At least 2GB of available disk space

## Quick Start

### 1. Clone and Navigate
```bash
git clone <repository-url>
cd ecommerce-backend
```

### 2. Configure Environment Variables
```bash
# Copy the example environment file
cp .env.docker.example .env

# Edit the .env file with your preferred values
nano .env  # or use your preferred editor
```

### 3. Start the Application
```bash
# Start all services (database + application)
docker-compose up -d

# Or start with build (first time or after code changes)
docker-compose up --build -d
```

### 4. Verify Everything is Running
```bash
# Check service status
docker-compose ps

# View logs
docker-compose logs -f app
```

The application will be available at:
- **API**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **API Documentation**: http://localhost:8080/swagger-ui.html (if Swagger is configured)

### 5. Stop the Application
```bash
# Stop all services
docker-compose down

# Stop and remove volumes (clears database)
docker-compose down -v
```

## Configuration

### Environment Variables (.env file)

#### Required Variables
```env
# Database
DATABASE_NAME=ecommerce_db
DATABASE_USERNAME=ecommerce_user
DATABASE_PASSWORD=your_secure_password

# JWT Security
JWT_SECRET=your_256_bit_secret_key_here
```

#### Optional Variables
```env
# Application
APP_PORT=8080
SPRING_PROFILES_ACTIVE=docker

# Data Seeding
SEED_DATA=true  # Set to false for production

# External Services
R2_BUCKET_NAME=your_bucket
GEMINI_API_KEY=your_api_key
```

### Database Seeding

The application automatically seeds the database with sample data on first startup when `SEED_DATA=true`. This includes:

- Admin and customer users
- Product catalog (brands, categories, products)
- Sample orders and reviews
- Event and discount data

**Default Admin Credentials:**
- Email: `admin@ecommerce.com`
- Password: `admin123`

## Available Services

### Main Services
- **app**: Spring Boot application (port 8080)
- **postgres**: PostgreSQL database (port 5432)

### Optional Services (Development)
- **pgadmin**: Database management UI (port 5050)

To start with PGAdmin:
```bash
docker-compose --profile dev up -d
```

Access PGAdmin at http://localhost:5050 with:
- Email: `admin@ecommerce.local`
- Password: `admin123`

## Docker Compose Profiles

- **Default**: Starts app + postgres
- **dev**: Adds PGAdmin for database management

## Useful Commands

### Development
```bash
# View application logs
docker-compose logs -f app

# View database logs
docker-compose logs -f postgres

# Restart just the application
docker-compose restart app

# Rebuild and start (after code changes)
docker-compose up --build app

# Execute commands in running container
docker-compose exec app bash
docker-compose exec postgres psql -U ecommerce_user -d ecommerce_db
```

### Maintenance
```bash
# Check disk usage
docker system df

# Remove unused images/containers
docker system prune

# Update to latest images
docker-compose pull
docker-compose up -d
```

### Database Operations
```bash
# Backup database
docker-compose exec postgres pg_dump -U ecommerce_user ecommerce_db > backup.sql

# Restore database
docker-compose exec -T postgres psql -U ecommerce_user ecommerce_db < backup.sql

# Reset database (WARNING: Deletes all data)
docker-compose down -v
docker-compose up -d
```

## Troubleshooting

### Common Issues

#### Port Already in Use
```bash
# Check what's using port 8080
lsof -i :8080

# Kill the process or change APP_PORT in .env
```

#### Database Connection Issues
```bash
# Check if database is ready
docker-compose exec postgres pg_isready -U ecommerce_user

# View database logs
docker-compose logs postgres
```

#### Application Won't Start
```bash
# Check application logs
docker-compose logs app

# Check if all required environment variables are set
docker-compose config
```

#### Out of Memory
```bash
# Check resource usage
docker stats

# Adjust memory limits in docker-compose.yml if needed
```

### Health Checks

The application includes health checks:
- **Database**: Automatic readiness check
- **Application**: HTTP health endpoint at `/actuator/health`

### Performance Tuning

For production use:
1. Set `SEED_DATA=false`
2. Use strong passwords
3. Configure proper resource limits
4. Enable SSL/TLS
5. Use a reverse proxy (nginx)

## File Structure

```
ecommerce-backend/
├── Dockerfile              # Multi-stage application build
├── docker-compose.yml      # Service orchestration
├── docker-entrypoint.sh    # Application startup script
├── .env.docker.example     # Environment template
├── .dockerignore           # Build context exclusions
├── docker/
│   └── init.sql            # Database initialization
└── DOCKER.md              # This documentation
```

## Production Deployment

For production deployment:

1. Use a production-grade environment file
2. Enable SSL/HTTPS
3. Set up proper monitoring
4. Configure backup strategies
5. Use Docker secrets for sensitive data
6. Implement proper logging and monitoring

## Support

If you encounter issues:
1. Check the logs: `docker-compose logs`
2. Verify environment configuration
3. Ensure all required ports are available
4. Check system resources (memory, disk)

For additional help, refer to the main project documentation or create an issue in the project repository.