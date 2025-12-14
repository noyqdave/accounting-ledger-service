# PostgreSQL Integration Testing with Testcontainers

## Overview

This project uses **Testcontainers** to run integration tests against a real PostgreSQL database running in Docker. This ensures your persistence layer works correctly with a production-like database.

## Prerequisites

1. **Docker Desktop** must be installed and running
2. **Docker socket** must be accessible

## Setup

### macOS with Docker Desktop

Docker Desktop on macOS uses a specific socket path. To run integration tests, you need to set the `DOCKER_HOST` environment variable:

```bash
export DOCKER_HOST=unix://$HOME/.docker/run/docker.sock
```

Or add it to your shell profile (`~/.zshrc` or `~/.bash_profile`):

```bash
echo 'export DOCKER_HOST=unix://$HOME/.docker/run/docker.sock' >> ~/.zshrc
source ~/.zshrc
```

### Verify Docker is Working

```bash
docker ps
docker pull postgres:15-alpine
```

## Running Integration Tests

Once Docker is configured, run the PostgreSQL integration test:

```bash
mvn test -Dtest=PostgreSQLIntegrationTest
```

**Note:** The first run will download the PostgreSQL Docker image (~100MB), which may take a minute.

## Test Details

The `PostgreSQLIntegrationTest` verifies:
- Transaction persistence and retrieval
- Multiple transactions handling
- BigDecimal precision preservation

## Troubleshooting

### "Could not find a valid Docker environment"

1. Ensure Docker Desktop is running
2. Set `DOCKER_HOST` environment variable (see Setup above)
3. Verify with: `docker ps`

### Test fails with connection errors

- Check Docker Desktop is fully started (not just installed)
- Verify the socket path: `ls -la ~/.docker/run/docker.sock`
- Try: `docker context use desktop-linux`

## Alternative: Skip Integration Tests

If you don't have Docker available, you can skip integration tests:

```bash
mvn test -Dtest=PostgreSQLIntegrationTest -DskipTests
```

Or exclude them in your IDE test configuration.
