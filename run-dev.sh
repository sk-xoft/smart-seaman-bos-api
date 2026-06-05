#!/bin/sh
set -e

# Start MySQL if not running
#if ! docker ps --format '{{.Names}}' | grep -q "^smart-seaman-mysql$"; then
#  echo "Starting MySQL..."
#  docker compose -f docker-compose.dev.yml up -d
#  echo "Waiting for MySQL to be ready..."
#  sleep 5
#fi

# Load .env if exists
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
fi

./mvnw spring-boot:run
