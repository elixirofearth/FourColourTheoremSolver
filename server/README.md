# Four Colour Theorem Solver - Spring Boot Backend

A Spring Boot microservices backend for the Four Colour Theorem Solver application.

## Architecture

5 Spring Boot microservices + Python solver:

- **API Gateway** (`8080`) - Main entry point, routing, authentication, Redis caching
- **Authentication Service** (`8081`) - User auth, JWT tokens, PostgreSQL
- **Map Storage Service** (`8083`) - CRUD operations, MongoDB
- **Logger Service** (`8084`, gRPC `50001`) - Centralized logging, Kafka messaging
- **Solver Service** (`8082`) - Python service for graph coloring algorithms

### Infrastructure

- **PostgreSQL**: User data and sessions
- **MongoDB**: Map storage and logs
- **Redis**: Caching and session storage
- **Kafka**: Message queuing for logs
- **Zookeeper**: Kafka coordination

## Quick Start

### Prerequisites

- Java 21+, Maven 3.8+, Docker Desktop, Python 3.8+

### Fastest Setup

```bash
cd server
make build
make up
```

Access at `http://localhost:8080`

## Development Setup

### 1. Build Project

```bash
cd server
mvn clean install
```

### 2. Start Infrastructure

```bash
docker-compose up -d postgres mongo redis zookeeper kafka
```

### 3. Start Services (in order)

```bash
# Terminal 1 - Logger Service
cd logger-service && mvn spring-boot:run

# Terminal 2 - Authentication Service
cd authentication-service && mvn spring-boot:run

# Terminal 3 - Map Storage Service
cd map-storage-service && mvn spring-boot:run

# Terminal 4 - Solver Service (Python)
cd solver-service && python app.py

# Terminal 5 - API Gateway
cd api-gateway-service && mvn spring-boot:run
```

### 4. Verify Setup

```bash
# Check all services
curl http://localhost:8080/healthcheck/services

# Test registration
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"test","email":"test@example.com","password":"password123"}'
```

## Health Checks

### Comprehensive Health Check

```bash
curl http://localhost:8080/healthcheck/services
```

**Response (200 OK):**

```json
{
  "gateway": "OK",
  "authentication-service": "OK",
  "map-storage-service": "OK",
  "solver-service": "OK"
}
```

### Individual Service Checks

```bash
curl http://localhost:8080/healthcheck               # API Gateway
curl http://localhost:8081/auth/healthcheck          # Authentication
curl http://localhost:8083/api/v1/maps/healthcheck   # Map Storage
curl http://localhost:8082/health                    # Solver Service
```

## API Endpoints

### Authentication (via Gateway)

- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/logout` - User logout

### Map Operations (protected)

- `POST /api/v1/maps/color` - Color a map
- `POST /api/v1/maps` - Save a map
- `GET /api/v1/maps?userId={id}` - Get user maps
- `GET /api/v1/maps/{id}` - Get specific map
- `DELETE /api/v1/maps/{id}` - Delete a map

## Configuration

### Environment Variables

#### Authentication Service

- `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`
- `JWT_SECRET`, `LOGGER_SERVICE_URL`

#### Map Storage Service

- `MONGO_URI`, `MONGO_DB`, `LOGGER_SERVICE_URL`

#### Logger Service

- `MONGO_URI`, `MONGO_DB`, `KAFKA_BOOTSTRAP_SERVERS`
- `KAFKA_GROUP_ID`

#### API Gateway Service

- `COLORING_SERVICE_URL`, `AUTHENTICATION_SERVICE_URL`, `MAP_STORAGE_SERVICE_URL`
- `REDIS_HOST`, `REDIS_PORT`

## Docker Deployment

### Full Setup

```bash
docker-compose up --build
```

### Infrastructure Only

```bash
docker-compose up -d postgres mongo redis zookeeper kafka
# Then run Spring services locally
```

## Makefile Commands

```bash
make build      # Build all services
make up         # Start everything
make up_build   # Start in background
make logs       # View logs
make down       # Stop everything
make clean      # Clean containers/images
make test       # Run tests

# Individual service builds
make build_gateway
make build_auth
make build_map_storage
make build_logger

# Individual service runs
make run_logger_service
make run_solver_service
make run_map_storage_service
make run_api_gateway_service
make run_authentication_service
```

## Troubleshooting

### Common Issues

**Port conflicts:**

```bash
lsof -i :8080  # Find process using port
```

**Database connection:**

```bash
docker-compose ps postgres
docker-compose logs postgres
```

**Kafka connection:**

```bash
docker-compose ps kafka
docker-compose logs kafka
```

**Health check failures:**

```bash
curl http://localhost:8080/healthcheck/services
docker-compose logs [service-name]
```

### Service Startup Order

1. Infrastructure: `docker-compose up -d postgres mongo redis zookeeper kafka`
2. Logger Service (other services depend on it)
3. Remaining services in any order

### Development Tips

- Use `spring-boot-devtools` for hot reload
- Connect to PostgreSQL: `localhost:5432`, MongoDB: `localhost:27017`
- Redis: `localhost:6379`
- Kafka: `localhost:9092`

## Key Features

- **Security**: JWT authentication, BCrypt password hashing, CORS
- **Data Persistence**: PostgreSQL (users), MongoDB (maps/logs)
- **Caching**: Redis for session storage and API caching
- **Centralized Logging**: gRPC logger service with Kafka messaging
- **Error Handling**: Comprehensive exception handling
- **Monitoring**: Health checks, structured logging
- **Shared Code**: Common module with DTOs and shared services

## Testing

The project includes comprehensive test suites for all services with multiple testing strategies:

### Test Types

- **Unit Tests**: Individual component testing with mocked dependencies
- **Integration Tests**: End-to-end testing with real databases and external services
- **Security Tests**: Authentication, authorization, and input validation testing
- **Performance Tests**: Load testing and performance benchmarking
- **Repository Tests**: Database interaction testing with Testcontainers

### Test Coverage

#### Spring Boot Services

- **API Gateway Service**: Gateway routing, caching, authentication proxy
- **Authentication Service**: User registration, login, JWT token management
- **Map Storage Service**: CRUD operations, MongoDB integration
- **Logger Service**: gRPC logging, Kafka messaging, MongoDB persistence
- **Common Module**: DTOs, shared services, validation

#### Python Solver Service

- **Algorithm Tests**: Graph coloring algorithm correctness
- **Integration Tests**: API endpoints, external service communication
- **Security Tests**: Input validation, authentication, authorization
- **Performance Tests**: Load testing, memory usage, response times

### Running Tests

#### All Tests

```bash
# Run all Java tests
mvn test

# Run all Python tests
cd solver-service && python -m pytest tests/
```

#### Individual Service Tests

```bash
# Java services
make test_common
make test_api_gateway
make test_authentication
make test_map_storage
make test_logger

# Python solver service
make test_solver
```

#### Test with Coverage

```bash
# Java services with coverage
mvn test jacoco:report

# Python solver with coverage
cd solver-service && python -m pytest tests/ --cov=app --cov-report=html
```

### Test Infrastructure

#### Java Services

- **JUnit 5**: Primary testing framework
- **Mockito**: Mocking and stubbing
- **Testcontainers**: Database containers for integration tests
- **Spring Boot Test**: Application context testing
- **TestRestTemplate**: HTTP client testing

#### Python Solver Service

- **pytest**: Testing framework
- **pytest-cov**: Coverage reporting
- **Flask Test Client**: HTTP endpoint testing
- **unittest.mock**: Mocking and patching

### Test Databases

#### Integration Test Databases

- **H2**: In-memory database for authentication service tests
- **MongoDB**: Testcontainers for map storage and logger services
- **Kafka**: Testcontainers for logger service messaging tests
- **Redis**: Mocked for API gateway caching tests

### Test Configuration

#### Java Services

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  data:
    mongodb:
      uri: mongodb://localhost:27017/testdb
  kafka:
    bootstrap-servers: localhost:9092
```

#### Python Solver Service

```python
# requirements_test.txt
pytest==8.4.1
pytest-cov
Flask==2.3.3
networkx==3.5
numpy==1.26.4
```

### Test Categories

#### Unit Tests

- Service layer business logic
- Repository data access patterns
- Controller request/response handling
- Utility function validation

#### Integration Tests

- Full HTTP request/response cycles
- Database persistence and retrieval
- External service communication
- Authentication and authorization flows

#### Security Tests

- Input validation and sanitization
- Authentication token verification
- Authorization role checking
- SQL injection prevention
- XSS protection

#### Performance Tests

- Response time benchmarking
- Memory usage monitoring
- Concurrent request handling
- Database query optimization

### Test Utilities

#### Common Test Utilities

- **TestUtils.java**: Shared test data and helper methods
- **TestEntityManager**: JPA entity management for tests
- **MockRestServiceServer**: HTTP service mocking
- **TestRestTemplate**: HTTP client for integration tests

#### Test Data Management

- **@BeforeEach**: Test setup and teardown
- **@DirtiesContext**: Context isolation between tests
- **@Transactional**: Database transaction management
- **@ActiveProfiles("test")**: Test-specific configuration

### Continuous Integration

#### Test Execution

```bash
# CI/CD pipeline commands
make test              # Run all tests
make test_solver       # Run Python tests
make test_common       # Run shared module tests
```

#### Test Reports

- JUnit XML reports for Java tests
- pytest HTML reports for Python tests
- JaCoCo coverage reports
- Test execution time tracking

### Debugging Tests

#### Java Tests

```bash
# Run with debug logging
mvn test -Dspring.profiles.active=test -Dlogging.level.com.fourcolour=DEBUG

# Run specific test class
mvn test -Dtest=AuthenticationServiceTest

# Run with coverage
mvn test jacoco:report
```

#### Python Solver Service Tests

```bash
# Navigate to solver service
cd solver-service

# Activate virtual environment
source venv/bin/activate  # macOS/Linux
# or venv\Scripts\activate  # Windows

# Install test dependencies (one-time)
pip install -r requirements_test.txt

# Generate protobuf files (one-time)
python -m grpc_tools.protoc --python_out=. --grpc_python_out=. --proto_path=proto/logs proto/logs/logger.proto
mv logger_pb2.py proto/logs/ && mv logger_pb2_grpc.py proto/logs/

# Run all tests
pytest

# Run with verbose output
pytest -v

# Run specific test files
pytest tests/test_app.py
pytest tests/test_coloring.py
pytest tests/test_algorithms.py
pytest tests/test_integration.py
pytest tests/test_performance.py
pytest tests/test_security.py

# Run specific test methods
pytest tests/test_app.py::TestSolverApp::test_health_endpoint

# Run with coverage
pytest --cov=app tests/

# Run tests in parallel
pytest -n auto
```

**Test Categories (126 total tests):**

- **App Tests** (32): Flask endpoints, API validation
- **Coloring Tests** (24): Graph coloring algorithms, CSP solver
- **Algorithm Tests** (20): Image processing algorithms
- **Integration Tests** (13): End-to-end testing
- **Performance Tests** (17): Scalability and performance
- **Security Tests** (20): Vulnerability and security testing

### Test Best Practices

1. **Isolation**: Each test is independent and doesn't affect others
2. **Mocking**: External dependencies are mocked for unit tests
3. **Real Databases**: Integration tests use real database containers
4. **Cleanup**: Tests clean up after themselves
5. **Naming**: Clear, descriptive test method names
6. **Documentation**: Tests serve as living documentation

## Migration from Go

This Spring Boot backend maintains:

- ✅ Identical API contracts
- ✅ Same database schemas
- ✅ Compatible authentication
- ✅ CORS support
- ✅ Docker deployment
- ✅ Complete logging system

## Quick Reference

### Essential Commands

```bash
# Health check
curl http://localhost:8080/healthcheck/services

# Start all services
docker-compose up -d --build

# View logs
docker-compose logs -f [service-name]

# Stop services
docker-compose down
```

### Service URLs

- API Gateway: `http://localhost:8080`
- Authentication: `http://localhost:8081`
- Map Storage: `http://localhost:8083`
- Solver: `http://localhost:8082`
- Logger: `http://localhost:8084` (gRPC: `50001`)

### Infrastructure URLs

- PostgreSQL: `localhost:5432`
- MongoDB: `localhost:27017`
- Redis: `localhost:6379`
- Kafka: `localhost:9092`
- Zookeeper: `localhost:2181`
