FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy Maven wrapper and pom files first for dependency caching
COPY ../pom.xml /app/parent-pom.xml
COPY ../common/pom.xml /app/common/pom.xml
COPY pom.xml /app/pom.xml

# Copy Maven wrapper
COPY ../mvnw /app/mvnw
COPY ../.mvn /app/.mvn

# Build dependencies first (for caching)
RUN ./mvnw dependency:go-offline -f /app/pom.xml

# Copy source code
COPY ../common/src /app/common/src
COPY src /app/src

# Build the application
RUN ./mvnw clean package -f /app/pom.xml -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/api-gateway-service-1.0.0.jar"] 