#!/bin/bash
# Helper script to run PostgreSQL integration tests with proper Docker configuration

# Set DOCKER_HOST for macOS Docker Desktop
export DOCKER_HOST=unix://$HOME/.docker/run/docker.sock

# Verify Docker is accessible
if ! docker ps > /dev/null 2>&1; then
    echo "Error: Docker is not accessible. Please ensure Docker Desktop is running."
    echo "Try: docker ps"
    exit 1
fi

# Run the integration test
echo "Running PostgreSQL integration tests..."
mvn test -Dtest=PostgreSQLIntegrationTest
