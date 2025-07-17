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

## Getting Started

### Prerequisites

Before running the application locally, ensure you have the following installed:

- **Java 21+**: [Download OpenJDK](https://openjdk.org/projects/jdk/21/)
- **Maven 3.8+**: [Download Maven](https://maven.apache.org/download.cgi)
- **Docker Desktop**: [Download Docker](https://www.docker.com/products/docker-desktop/)
- **Python 3.8+**: For the solver service (if running without Docker)
- **Git**: For cloning the repository

Verify your installations:

```bash
java --version
mvn --version
docker --version
python --version
```

### Quick Start (Recommended)

The fastest way to get everything running:

```bash
# 1. Navigate to the spring_server directory
cd spring_server

# 2. Build all services
make build

# 3. Start everything with Docker Compose
make up

# 4. Verify all services are running
make logs
```

Access the application at `http://localhost:8080`

### Local Development Setup

For development work where you want to run services individually:

#### Step 1: Clone and Build

```bash
# Clone the repository (if not already done)
git clone <repository-url>
cd FourColourTheoremSolver/spring_server

# Build the entire project
mvn clean install
```

#### Step 2: Start Infrastructure Services

Start the required databases and message queue:

```bash
# Start PostgreSQL, MongoDB, and RabbitMQ
docker-compose up -d postgres mongo rabbitmq

# Verify they're running
docker-compose ps
```

Wait for all services to be healthy (usually 30-60 seconds).

#### Step 3: Set Environment Variables (Optional)

For local development, default values work, but you can customize:

```bash
# PostgreSQL (Authentication Service)
export DB_HOST=localhost
export DB_PORT=5432
export DB_USER=postgres
export DB_PASSWORD=password
export DB_NAME=users
export JWT_SECRET=your-secret-key-here

# MongoDB (Map Storage & Logger Services)
export MONGO_URI=mongodb://localhost:27017
export MONGO_DB_MAPS=mapstore
export MONGO_DB_LOGS=logs

# RabbitMQ (Logger Service)
export RABBITMQ_HOST=localhost
export RABBITMQ_PORT=5672
export RABBITMQ_USERNAME=guest
export RABBITMQ_PASSWORD=guest

# Service URLs (API Gateway)
export AUTHENTICATION_SERVICE_URL=http://localhost:8081
export MAP_STORAGE_SERVICE_URL=http://localhost:8083
export COLORING_SERVICE_URL=http://localhost:8082
export LOGGER_SERVICE_URL=localhost:50001
```

#### Step 4: Start Services in Order

**Important**: Start services in this order due to dependencies:

```bash
# Terminal 1 - Logger Service (other services depend on this)
cd logger-service
mvn spring-boot:run

# Wait for "gRPC server started on port 50001" message
```

```bash
# Terminal 2 - Authentication Service
cd authentication-service
mvn spring-boot:run

# Wait for "Started AuthenticationServiceApplication" message
```

```bash
# Terminal 3 - Map Storage Service
cd map-storage-service
mvn spring-boot:run

# Wait for "Started MapStorageServiceApplication" message
```

```bash
# Terminal 4 - Solver Service (Python)
cd solver-service
# Install dependencies (first time only)
pip install -r requirements.txt
python app.py

# Wait for "Solver service running on port 8082" message
```

```bash
# Terminal 5 - API Gateway (start last)
cd api-gateway-service
mvn spring-boot:run

# Wait for "Started ApiGatewayServiceApplication" message
```

#### Step 5: Verify Everything is Running

Check that all services are responding:

```bash
# Check service health
curl http://localhost:8080/healthcheck  # API Gateway
curl http://localhost:8081/healthcheck  # Authentication
curl http://localhost:8083/healthcheck  # Map Storage
curl http://localhost:8084/healthcheck  # Logger Service
curl http://localhost:8082/healthcheck  # Solver Service

# Test basic functionality
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"password123"}'
```

### Alternative: IDE Setup

For development in IntelliJ IDEA or Eclipse:

1. **Import the project**: Open the `pom.xml` in the spring_server directory
2. **Configure Run Configurations**: Create separate run configurations for each service
3. **Set Working Directory**: Ensure each service runs from its own module directory
4. **Start Infrastructure**: Use `docker-compose up -d postgres mongo rabbitmq`
5. **Run Services**: Start each service using your IDE's run configurations

### Docker Deployment

For production-like environment or if you prefer containers:

#### Full Docker Setup

```bash
# Build and start all services
docker-compose up --build

# Or run in background
docker-compose up -d --build

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

#### Partial Docker Setup (Infrastructure Only)

Run infrastructure in Docker but services locally:

```bash
# Start only databases and RabbitMQ
docker-compose up -d postgres mongo rabbitmq

# Then run Spring services locally as described above
```

### Using the Makefile

The project includes a Makefile for common operations:

```bash
# Build all services
make build

# Start everything
make up

# Start in background
make up_build

# View logs
make logs

# Stop everything
make down

# Clean containers and images
make clean

# Run tests
make test
```

### Troubleshooting

#### Common Issues

**Service won't start - Port already in use:**

```bash
# Find what's using the port
lsof -i :8080  # Replace with your port
# Kill the process or use different ports
```

**Database connection issues:**

```bash
# Check if PostgreSQL is running
docker-compose ps postgres
# Check logs
docker-compose logs postgres
# Restart if needed
docker-compose restart postgres
```

**gRPC connection errors:**

```bash
# Ensure logger service is running first
# Check gRPC port is not blocked
telnet localhost 50001
```

**Maven build failures:**

```bash
# Clear local repository and rebuild
rm -rf ~/.m2/repository/com/fourcolour
mvn clean install -U
```

#### Service Startup Order Issues

If services fail to connect to each other:

1. Stop all services
2. Start infrastructure: `docker-compose up -d postgres mongo rabbitmq`
3. Wait 30 seconds for databases to initialize
4. Start logger service first (other services depend on it)
5. Start remaining services in any order

#### Log Files and Debugging

```bash
# View service logs
docker-compose logs [service-name]

# Follow logs in real-time
docker-compose logs -f [service-name]

# Spring Boot debug mode
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

### Development Tips

1. **Hot Reload**: Use `spring-boot-devtools` dependency for automatic restarts
2. **Database Tools**: Connect to PostgreSQL at `localhost:5432` and MongoDB at `localhost:27017`
3. **API Testing**: Use tools like Postman or curl to test endpoints
4. **Log Monitoring**: Check RabbitMQ management UI at `http://localhost:15672` (guest/guest)

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
