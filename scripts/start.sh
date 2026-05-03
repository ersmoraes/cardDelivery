#!/usr/bin/env bash
set -e
echo "Subindo CardDeliveryHub..."
docker compose up -d --build
echo "Aguardando aplicacao ficar saudavel..."
until curl -sf http://localhost:8082/actuator/health > /dev/null 2>&1; do
  echo "  ...ainda aguardando"
  sleep 3
done
echo "Aplicacao no ar em http://localhost:8082"
echo ""
echo "pgAdmin:    http://localhost:5050 (admin@carddelivery.local / admin)"
echo "RabbitMQ:   http://localhost:15672 (guest / guest)"
echo "Wiremock:   http://localhost:8081/__admin/"
echo "SonarQube:  http://localhost:9000 (admin / admin)"
echo "Postgres:   localhost:5432 (app / app)"
