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
  -d '{"username":"test","email":"test@example.com","password":"password123"}'
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

```bash
mvn test
```

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
