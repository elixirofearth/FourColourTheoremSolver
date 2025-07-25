#!/bin/bash

# Four Colour Theorem Solver - Kubernetes Cleanup Script
# This script removes all Kubernetes resources

set -e

echo "🧹 Starting cleanup of Four Colour Theorem Solver..."

# Delete all deployments and services
echo "🗑️  Deleting deployments and services..."
kubectl delete -f api-gateway-service.yaml --ignore-not-found=true
kubectl delete -f solver-service.yaml --ignore-not-found=true
kubectl delete -f map-storage-service.yaml --ignore-not-found=true
kubectl delete -f authentication-service.yaml --ignore-not-found=true
kubectl delete -f logger-service.yaml --ignore-not-found=true
kubectl delete -f kafka.yaml --ignore-not-found=true
kubectl delete -f redis.yaml --ignore-not-found=true
kubectl delete -f mongo.yaml --ignore-not-found=true
kubectl delete -f postgres.yaml --ignore-not-found=true

# Delete ConfigMap
echo "🗑️  Deleting ConfigMap..."
kubectl delete -f configmap.yaml --ignore-not-found=true

# Delete namespace (this will delete all resources in the namespace)
echo "🗑️  Deleting namespace..."
kubectl delete -f namespace.yaml --ignore-not-found=true

echo "✅ Cleanup completed successfully!" 