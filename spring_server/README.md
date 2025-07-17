# Four Colour Theorem Solver - Spring Boot Backend

This is a Spring Boot migration of the original Go microservices backend. It maintains the same API contracts and functionality while leveraging the Spring Boot ecosystem.

## Architecture

The system consists of 5 Spring Boot microservices plus the original Python solver service:

### Spring Boot Services

1. **API Gateway Service** (`port 8080`)

   - Main entry point for all requests
   - Handles CORS and request routing
   - Implements authentication middleware
   - Routes requests to appropriate services

2. **Authentication Service** (`port 8081`)

   - User registration and login
   - JWT token generation and validation
   - Session management with PostgreSQL
   - Password hashing with BCrypt

3. **Map Storage Service** (`port 8083`)

   - CRUD operations for maps
   - MongoDB integration
   - Map metadata and image data storage

4. **Logger Service** (`port 8084`, gRPC `port 50001`)

   - gRPC-based centralized logging
   - RabbitMQ message queuing
   - MongoDB log storage
   - Event tracking across all services

5. **Solver Service** (`port 8082`) - **Original Python service**
   - Graph coloring algorithms using ASP
   - Image processing and segmentation
   - Four-color theorem implementation

### Infrastructure

- **PostgreSQL**: User data and sessions
- **MongoDB**: Map storage, metadata, and logs
- **RabbitMQ**: Message queuing for log events

## API Endpoints

### Authentication (via Gateway)

- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/logout` - User logout

### Map Operations (via Gateway, protected)

- `POST /api/v1/maps/color` - Color a map using the solver
- `POST /api/v1/maps` - Save a map
- `GET /api/v1/maps?userId={id}` - Get maps for user
- `GET /api/v1/maps/{id}` - Get specific map
- `DELETE /api/v1/maps/{id}` - Delete a map

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker and Docker Compose

### Development Setup

1. **Build the project:**

   ```bash
   mvn clean install
   ```

2. **Start infrastructure services:**

   ```bash
   docker-compose up postgres mongo rabbitmq
   ```

3. **Run services locally:**

   ```bash
   # Terminal 1 - Logger Service
   cd logger-service
   mvn spring-boot:run

   # Terminal 2 - Authentication Service
   cd authentication-service
   mvn spring-boot:run

   # Terminal 3 - Map Storage Service
   cd map-storage-service
   mvn spring-boot:run

   # Terminal 4 - API Gateway
   cd api-gateway-service
   mvn spring-boot:run

   # Terminal 5 - Solver Service (Python)
   cd ../server/solver-service
   python app.py
   ```

### Docker Deployment

1. **Build and start all services:**

   ```bash
   docker-compose up --build
   ```

2. **Stop services:**
   ```bash
   docker-compose down
   ```

### Configuration

Each service can be configured via environment variables:

#### Authentication Service

- `DB_HOST`: PostgreSQL host (default: localhost)
- `DB_PORT`: PostgreSQL port (default: 5432)
- `DB_USER`: Database username (default: postgres)
- `DB_PASSWORD`: Database password (default: password)
- `DB_NAME`: Database name (default: users)
- `JWT_SECRET`: JWT signing secret
- `LOGGER_SERVICE_URL`: Logger service gRPC endpoint

#### Map Storage Service

- `MONGO_URI`: MongoDB connection string
- `MONGO_DB`: MongoDB database name (default: mapstore)
- `LOGGER_SERVICE_URL`: Logger service gRPC endpoint

#### Logger Service

- `MONGO_URI`: MongoDB connection string for logs
- `MONGO_DB`: MongoDB database name (default: logs)
- `RABBITMQ_HOST`: RabbitMQ host
- `RABBITMQ_PORT`: RabbitMQ port
- `RABBITMQ_USERNAME`: RabbitMQ username
- `RABBITMQ_PASSWORD`: RabbitMQ password

#### API Gateway Service

- `COLORING_SERVICE_URL`: Solver service URL
- `AUTHENTICATION_SERVICE_URL`: Auth service URL
- `MAP_STORAGE_SERVICE_URL`: Map storage service URL

## Key Features

### Security

- JWT-based authentication with session tracking
- Password hashing using BCrypt
- Authorization middleware for protected endpoints
- CORS configuration for frontend integration

### Data Persistence

- PostgreSQL for user data with JPA/Hibernate
- MongoDB for map storage with Spring Data MongoDB
- MongoDB for centralized logging
- Automatic schema creation and migration

### Centralized Logging

- gRPC-based logger service for high-performance logging
- RabbitMQ queuing with automatic exchange/queue setup
- Structured event logging across all services
- MongoDB storage for log persistence and querying

### Error Handling

- Comprehensive exception handling
- Consistent error response format
- Request validation with Bean Validation

### Messaging & Communication

- gRPC for high-performance inter-service communication
- RabbitMQ for reliable message queuing
- RestTemplate for HTTP service communication

## Migration from Go Services

This Spring Boot backend is a direct migration from the original Go services with:

✅ **Identical API contracts** - Same endpoints, request/response formats
✅ **Same database schemas** - PostgreSQL and MongoDB structures preserved  
✅ **Compatible authentication** - JWT tokens work across systems
✅ **CORS support** - Frontend integration unchanged
✅ **Docker deployment** - Same port mappings and service names
✅ **Complete logging** - gRPC logger service with RabbitMQ and MongoDB

### Differences from Go Version

- Uses Spring Boot auto-configuration instead of manual setup
- JPA/Hibernate for database operations instead of raw SQL
- Spring Data MongoDB instead of Go MongoDB driver
- RestTemplate for service communication instead of Go HTTP client
- Spring Security for password hashing instead of Go bcrypt
- gRPC Spring Boot starter instead of manual gRPC setup
- Spring AMQP for RabbitMQ instead of Go AMQP

## Logging Events

The system logs the following events:

### Authentication Service

- `user_registered` - New user registration
- `user_login` - Successful login
- `login_failed` - Failed login attempts
- `user_logout` - User logout
- `session_created` - New session creation
- `token_refreshed` - Token refresh

### Map Storage Service

- `map_created` - New map saved
- `map_deleted` - Map deletion

### Solver Service

- `map_coloring_completed` - Map coloring process completed

## Testing

Run tests for all services:

```bash
mvn test
```

## Monitoring

- Health checks available at `/healthcheck` for each service
- gRPC health checks for logger service
- Centralized logging via logger service
- Spring Boot Actuator endpoints can be enabled for production monitoring
- Logs available via Docker: `docker-compose logs -f [service-name]`

## Future Enhancements

- [x] Complete gRPC logger service integration
- [x] RabbitMQ message queuing for logs
- [ ] Add Spring Boot Actuator for monitoring
- [ ] Implement service discovery with Eureka
- [ ] Add distributed tracing with Zipkin
- [ ] Kubernetes deployment manifests
- [ ] Integration tests with Testcontainers
- [ ] Log analytics and search capabilities
