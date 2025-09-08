#!/bin/bash

# Docker entrypoint script for E-commerce Spring Boot application
# This script handles startup logic including database seeding

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to log messages
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}"
}

warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}"
}

info() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')] INFO: $1${NC}"
}

# Function to wait for database
wait_for_database() {
    log "Waiting for database to be ready..."
    
    # Extract database connection details from DATABASE_URL or use defaults
    DB_HOST="${DATABASE_HOST:-postgres}"
    DB_PORT="${DATABASE_PORT:-5432}"
    DB_NAME="${DATABASE_NAME:-ecommerce_db}"
    DB_USER="${DATABASE_USERNAME:-ecommerce_user}"
    
    # Wait for database to be ready
    while ! pg_isready -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" >/dev/null 2>&1; do
        info "Database is not ready yet... waiting 5 seconds"
        sleep 5
    done
    
    log "Database is ready!"
}

# Function to check if database seeding should run
should_seed_database() {
    # Check if SEED_DATA is explicitly set to true
    if [ "${SEED_DATA,,}" = "true" ]; then
        return 0
    fi
    
    # If not explicitly set, return false (don't seed)
    return 1
}

# Function to prepare application
prepare_application() {
    log "Preparing Spring Boot application..."
    
    # Set JVM options for better performance in containers
    export JAVA_OPTS="${JAVA_OPTS} -Xmx512m -Xms256m"
    export JAVA_OPTS="${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom"
    export JAVA_OPTS="${JAVA_OPTS} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-docker}"
    
    # Enable data seeding if requested
    if should_seed_database; then
        log "Database seeding is enabled (SEED_DATA=true)"
        export JAVA_OPTS="${JAVA_OPTS} -Dseed.data=true"
    else
        info "Database seeding is disabled"
    fi
    
    log "JVM Options: $JAVA_OPTS"
}

# Function to start application
start_application() {
    log "Starting E-commerce Spring Boot application..."
    
    # Start the Spring Boot application
    exec java $JAVA_OPTS -jar app.jar "$@"
}

# Function to handle graceful shutdown
cleanup() {
    log "Received shutdown signal. Performing cleanup..."
    # Add any cleanup logic here if needed
    exit 0
}

# Set up signal handlers
trap cleanup SIGTERM SIGINT

# Main execution
main() {
    log "=== E-commerce Backend Docker Container Starting ==="
    
    # Display environment info
    info "Spring Profile: ${SPRING_PROFILES_ACTIVE:-docker}"
    info "Database URL: ${DATABASE_URL:-jdbc:postgresql://postgres:5432/ecommerce_db}"
    info "Seed Data: ${SEED_DATA:-false}"
    
    # Wait for database to be ready
    wait_for_database
    
    # Prepare application
    prepare_application
    
    # Start the application
    start_application "$@"
}

# Run main function
main "$@"