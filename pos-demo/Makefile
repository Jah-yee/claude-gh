# Java builds run inside the Maven image, so only Docker is required.
# The Docker socket is mounted so Testcontainers can start Postgres and RabbitMQ.
MVN = docker run --rm --network host -u $$(id -u):$$(id -g) --group-add $$(stat -c %g /var/run/docker.sock) \
	-v /var/run/docker.sock:/var/run/docker.sock -v storelite-m2:/m2 -v "$(CURDIR)":/work -e HOME=/tmp \
	maven:3.9-eclipse-temurin-21 mvn -B -Dmaven.repo.local=/m2

.PHONY: up down logs test test-inventory test-sales test-web gen smoke m2

up:
	docker compose up --build -d --wait
	@echo "Register:    http://localhost:5173/register"
	@echo "Dashboard:   http://localhost:5173/dashboard"
	@echo "RabbitMQ UI: http://localhost:15672 (guest/guest)"

down:
	docker compose down -v

logs:
	docker compose logs -f inventory-service sales-service

test: test-inventory test-sales test-web

m2:
	@docker volume inspect storelite-m2 >/dev/null 2>&1 || \
		(docker volume create storelite-m2 >/dev/null && docker run --rm -v storelite-m2:/m2 alpine chown $$(id -u):$$(id -g) /m2)

test-inventory: m2
	$(MVN) -f /work/inventory-service/pom.xml verify

test-sales: m2
	$(MVN) -f /work/sales-service/pom.xml verify

test-web:
	cd web && npm ci && npm test

gen:
	cd web && npm run gen

smoke:
	./scripts/smoke.sh
