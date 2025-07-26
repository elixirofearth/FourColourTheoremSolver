#!/bin/bash

# Four Colour Theorem Solver - Application Deployment Script
# This script deploys only the application services to a Kubernetes cluster
# Note: Infrastructure services should be deployed first using deploy-infrastructure.sh

set -e

echo "🚀 Starting Four Colour Theorem Solver application deployment..."

# Check if infrastructure is ready
echo "🔍 Checking infrastructure readiness..."
if ! kubectl get deployment/postgres -n fourcolour >/dev/null 2>&1; then
    echo "❌ Error: PostgreSQL deployment not found. Please run deploy-infrastructure.sh first."
    exit 1
fi

if ! kubectl get deployment/mongo -n fourcolour >/dev/null 2>&1; then
    echo "❌ Error: MongoDB deployment not found. Please run deploy-infrastructure.sh first."
    exit 1
fi

if ! kubectl get deployment/redis -n fourcolour >/dev/null 2>&1; then
    echo "❌ Error: Redis deployment not found. Please run deploy-infrastructure.sh first."
    exit 1
fi

if ! kubectl get deployment/kafka -n fourcolour >/dev/null 2>&1; then
    echo "❌ Error: Kafka deployment not found. Please run deploy-infrastructure.sh first."
    exit 1
fi

echo "✅ Infrastructure services are available."

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

echo "✅ Application deployment completed successfully!"
echo ""
echo "📊 Application Status:"
kubectl get pods -n fourcolour -l app.kubernetes.io/component=application
echo ""
echo "🌐 Application Services:"
kubectl get services -n fourcolour -l app.kubernetes.io/component=application
echo ""
echo "🔗 To access the API Gateway:"
echo "   kubectl port-forward service/api-gateway-service 8080:8080 -n fourcolour"
echo ""
echo "📝 To view logs:"
echo "   kubectl logs -f deployment/api-gateway-service -n fourcolour" 