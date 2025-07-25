#!/bin/bash

# Four Colour Theorem Solver - Docker Build and Push Script
# This script builds and pushes all microservices to Docker Hub

set -e

DOCKER_USERNAME="aqtran"
VERSION="latest"

echo "🐳 Building and pushing Four Colour Theorem Solver microservices to Docker Hub..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

echo "🔨 Building and pushing microservices..."

# Build and push API Gateway Service
echo "  - API Gateway Service..."
cd ../api-gateway-service
docker build -t $DOCKER_USERNAME/spring-api-gateway-service:$VERSION -f Dockerfile ..
docker push $DOCKER_USERNAME/spring-api-gateway-service:$VERSION
cd ../k8s

# Build and push Authentication Service
echo "  - Authentication Service..."
cd ../authentication-service
docker build -t $DOCKER_USERNAME/spring-authentication-service:$VERSION -f Dockerfile ..
docker push $DOCKER_USERNAME/spring-authentication-service:$VERSION
cd ../k8s

# Build and push Map Storage Service
echo "  - Map Storage Service..."
cd ../map-storage-service
docker build -t $DOCKER_USERNAME/spring-map-storage-service:$VERSION -f Dockerfile ..
docker push $DOCKER_USERNAME/spring-map-storage-service:$VERSION
cd ../k8s

# Build and push Logger Service
echo "  - Logger Service..."
cd ../logger-service
docker build -t $DOCKER_USERNAME/spring-logger-service:$VERSION -f Dockerfile ..
docker push $DOCKER_USERNAME/spring-logger-service:$VERSION
cd ../k8s

# Build and push Solver Service
echo "  - Solver Service..."
cd ../solver-service
docker build -t $DOCKER_USERNAME/flask-solver-service:$VERSION -f Dockerfile ..
docker push $DOCKER_USERNAME/flask-solver-service:$VERSION
cd ../k8s

echo "✅ All microservices have been built and pushed to Docker Hub!"
echo ""
echo "📦 Images pushed:"
echo "  - aqtran/spring-api-gateway-service:$VERSION"
echo "  - aqtran/spring-authentication-service:$VERSION"
echo "  - aqtran/spring-map-storage-service:$VERSION"
echo "  - aqtran/spring-logger-service:$VERSION"
echo "  - aqtran/flask-solver-service:$VERSION"
echo ""
echo "🚀 You can now deploy to Kubernetes using:"
echo "   cd k8s && ./deploy.sh" 