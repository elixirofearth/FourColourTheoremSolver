FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy Maven wrapper and pom files first for dependency caching
COPY pom.xml /app/parent-pom.xml
COPY common/pom.xml /app/common/pom.xml
COPY api-gateway-service/pom.xml /app/pom.xml

# Copy Maven wrapper
COPY mvnw /app/mvnw
COPY .mvn /app/.mvn

# Install parent POM first (without building child modules)
RUN ./mvnw install -f /app/parent-pom.xml -DskipTests -N

# Copy source code first
COPY common/src /app/common/src
COPY api-gateway-service/src /app/src

# Install common library first
RUN ./mvnw clean install -f /app/common/pom.xml -DskipTests

# Build dependencies first (for caching)
RUN ./mvnw dependency:go-offline -f /app/pom.xml

# Build the application
RUN ./mvnw clean package -f /app/pom.xml -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/api-gateway-service-1.0.0.jar"] 