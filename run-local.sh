#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

./mvnw clean package
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
