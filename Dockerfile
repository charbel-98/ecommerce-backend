# Multi-stage Docker build for Spring Boot E-commerce Backend

# Stage 1: Build stage
FROM maven:3.9.5-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (for better caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre

# Add labels for better maintenance
LABEL maintainer="ecommerce-backend"
LABEL description="Spring Boot E-commerce Backend API"

# Install PostgreSQL client tools and curl for health checks
RUN apt-get update && \
    apt-get install -y postgresql-client curl && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd --gid 1001 appgroup && \
    useradd --uid 1001 --gid appgroup --shell /bin/bash --create-home appuser

# Set working directory
WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/target/*.jar app.jar

# Copy entrypoint script
COPY docker-entrypoint.sh /app/docker-entrypoint.sh

# Set ownership and permissions
RUN chown -R appuser:appgroup /app && \
    chmod +x /app/docker-entrypoint.sh

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Health check to ensure application is running
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Use entrypoint script
ENTRYPOINT ["/app/docker-entrypoint.sh"]