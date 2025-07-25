#!/bin/bash

# Four Colour Theorem Solver - Kubernetes Deployment Script
# This script deploys all microservices to a Kubernetes cluster

set -e

echo "🚀 Starting Four Colour Theorem Solver deployment..."

# Create namespace
echo "📦 Creating namespace..."
kubectl apply -f namespace.yaml

# Apply ConfigMap
echo "⚙️  Applying ConfigMap..."
kubectl apply -f configmap.yaml

# Deploy infrastructure services first
echo "🗄️  Deploying infrastructure services..."

echo "  - PostgreSQL..."
kubectl apply -f postgres.yaml

echo "  - MongoDB..."
kubectl apply -f mongo.yaml

echo "  - Redis..."
kubectl apply -f redis.yaml

echo "  - Kafka & Zookeeper..."
kubectl apply -f kafka.yaml

# Wait for infrastructure to be ready
echo "⏳ Waiting for infrastructure services to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/postgres -n fourcolour
kubectl wait --for=condition=available --timeout=300s deployment/mongo -n fourcolour
kubectl wait --for=condition=available --timeout=300s deployment/redis -n fourcolour
kubectl wait --for=condition=available --timeout=300s deployment/zookeeper -n fourcolour
kubectl wait --for=condition=available --timeout=300s deployment/kafka -n fourcolour

# Deploy application services
echo "🔧 Deploying application services..."

echo "  - Logger Service..."
kubectl apply -f logger-service.yaml

echo "  - Authentication Service..."
kubectl apply -f authentication-service.yaml

echo "  - Map Storage Service..."
kubectl apply -f map-storage-service.yaml

echo "  - Solver Service..."
kubectl apply -f solver-service.yaml

echo "  - API Gateway Service..."
kubectl apply -f api-gateway-service.yaml

# Wait for application services to be ready
echo "⏳ Waiting for application services to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/logger-service -n fourcolour
kubectl wait --for=condition=available --timeout=300s deployment/authentication-service -n fourcolour
kubectl wait --for=condition=available --timeout=300s deployment/map-storage-service -n fourcolour
kubectl wait --for=condition=available --timeout=300s deployment/solver-service -n fourcolour
kubectl wait --for=condition=available --timeout=300s deployment/api-gateway-service -n fourcolour

echo "✅ Deployment completed successfully!"
echo ""
echo "📊 Deployment Status:"
kubectl get pods -n fourcolour
echo ""
echo "🌐 Services:"
kubectl get services -n fourcolour
echo ""
echo "🔗 To access the API Gateway:"
echo "   kubectl port-forward service/api-gateway-service 8080:8080 -n fourcolour"
echo ""
echo "📝 To view logs:"
echo "   kubectl logs -f deployment/api-gateway-service -n fourcolour" 