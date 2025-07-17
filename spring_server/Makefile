## Build and deployment targets for Spring Boot microservices

## up: starts all containers in the background without forcing build
up:
	@echo "Starting Docker images..."
	docker compose up -d
	@echo "Docker images started!"

## up_build: stops docker compose (if running), builds all projects and starts docker compose
up_build: clean build
	@echo "Stopping docker images (if running...)"
	docker compose down
	@echo "Building (when required) and starting docker images..."
	docker compose up --build -d
	@echo "Docker images built and started!"

## down: stop docker compose
down:
	@echo "Stopping docker compose..."
	docker compose down
	@echo "Done!"

## clean: clean all maven projects
clean:
	@echo "Cleaning all Maven projects..."
	mvn clean
	@echo "Done!"

## build: build all maven projects
build:
	@echo "Building all Maven projects..."
	mvn clean install -DskipTests
	@echo "Done!"

## test: run tests for all projects
test:
	@echo "Running tests for all projects..."
	mvn test
	@echo "Done!"

## build_gateway: builds the API gateway service
build_gateway:
	@echo "Building API gateway service..."
	cd api-gateway-service && mvn clean package -DskipTests
	@echo "Done!"

## build_auth: builds the authentication service
build_auth:
	@echo "Building authentication service..."
	cd authentication-service && mvn clean package -DskipTests
	@echo "Done!"

## build_map_storage: builds the map storage service
build_map_storage:
	@echo "Building map storage service..."
	cd map-storage-service && mvn clean package -DskipTests
	@echo "Done!"

## build_logger: builds the logger service
build_logger:
	@echo "Building logger service..."
	cd logger-service && mvn clean package -DskipTests
	@echo "Done!"

## logs: view logs from all services
logs:
	@echo "Showing logs from all services..."
	docker compose logs -f

## logs_auth: view logs from authentication service
logs_auth:
	@echo "Showing logs from authentication service..."
	docker compose logs -f authentication-service

## logs_maps: view logs from map storage service
logs_maps:
	@echo "Showing logs from map storage service..."
	docker compose logs -f map-storage-service

## logs_gateway: view logs from API gateway service
logs_gateway:
	@echo "Showing logs from API gateway service..."
	docker compose logs -f api-gateway-service

## logs_logger: view logs from logger service
logs_logger:
	@echo "Showing logs from logger service..."
	docker compose logs -f logger-service

## help: shows this help message
help:
	@echo "Available targets:"
	@sed -n 's/^##//p' ${MAKEFILE_LIST} | column -t -s ':' | sed -e 's/^/ /' 