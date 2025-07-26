#!/bin/bash

# Four Colour Theorem Solver - Infrastructure Deployment Script
# This script deploys only the infrastructure services to a Kubernetes cluster

set -e

echo "🚀 Starting Four Colour Theorem Solver infrastructure deployment..."

# Create namespace
echo "📦 Creating namespace..."
kubectl apply -f namespace.yaml

# Apply ConfigMap
echo "⚙️  Applying ConfigMap..."
kubectl apply -f configmap.yaml

# Deploy infrastructure services
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

echo "✅ Infrastructure deployment completed successfully!"
echo ""
echo "📊 Infrastructure Status:"
kubectl get pods -n fourcolour -l app.kubernetes.io/component=infrastructure
echo ""
echo "🌐 Infrastructure Services:"
kubectl get services -n fourcolour -l app.kubernetes.io/component=infrastructure 