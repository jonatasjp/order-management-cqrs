#!/bin/bash
set -e

echo "Buildando cqrs-command..."
(cd cqrs-command && ./mvnw clean package -DskipTests)

echo "Buildando cqrs-query..."
(cd cqrs-query && ./mvnw clean package -DskipTests)

echo "Subindo Docker Compose..."
docker compose up --build