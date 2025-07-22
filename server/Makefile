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
	mvn clean install -DskipTests -T1 --fail-at-end
	@echo "Done!"

## test: run tests for all projects
test:
	@echo "Running tests for all projects..."
	mvn test
	@echo "Done!"

## test_solver: run tests for the solver service
test_solver:
	@echo "Running tests for solver service..."
	cd solver-service && source venv/bin/activate && python -m pytest tests/
	@echo "Done!"

## test_common: run tests for the common service
test_common:
	@echo "Running tests for common service..."
	cd common && mvn test
	@echo "Done!"

## test_api_gateway: run tests for the API gateway service
test_api_gateway:
	@echo "Running tests for API gateway service..."
	cd api-gateway-service && mvn test
	@echo "Done!"

## test_authentication: run tests for the authentication service
test_authentication:
	@echo "Running tests for authentication service..."
	cd authentication-service && mvn test
	@echo "Done!"

## test_map_storage: run tests for the map storage service
test_map_storage:
	@echo "Running tests for map storage service..."
	cd map-storage-service && mvn test
	@echo "Done!"

## test_logger: run tests for the logger service
test_logger:
	@echo "Running tests for logger service..."
	cd logger-service && mvn test
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

## run_logger_service: run the logger service
run_logger_service:
	@echo "Running logger service..."
	docker compose up -d --build logger-service

## run_solver_service: run the solver service
run_solver_service:
	@echo "Running solver service..."
	docker compose up -d --build solver-service

## run_map_storage_service: run the map storage service
run_map_storage_service:
	@echo "Running map storage service..."
	docker compose up -d --build map-storage-service

## run_api_gateway_service: run the API gateway service
run_api_gateway_service:
	@echo "Running API gateway service..."
	docker compose up -d --build api-gateway-service

## run_authentication_service: run the authentication service
run_authentication_service:
	@echo "Running authentication service..."
	docker compose up -d --build authentication-service

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

