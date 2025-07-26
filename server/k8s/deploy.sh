#!/bin/bash

# Four Colour Theorem Solver - Master Kubernetes Deployment Script
# This script deploys all microservices to a Kubernetes cluster
# It calls the infrastructure and application deployment scripts in sequence

set -e

echo "🚀 Starting Four Colour Theorem Solver deployment..."

# Check if the deployment scripts exist
if [ ! -f "deploy-infrastructure.sh" ]; then
    echo "❌ Error: deploy-infrastructure.sh not found in current directory"
    exit 1
fi

if [ ! -f "deploy-application.sh" ]; then
    echo "❌ Error: deploy-application.sh not found in current directory"
    exit 1
fi

# Make scripts executable
chmod +x deploy-infrastructure.sh
chmod +x deploy-application.sh

# Deploy infrastructure first
echo "📦 Deploying infrastructure services..."
./deploy-infrastructure.sh

echo ""
echo "⏳ Waiting 30 seconds for infrastructure to stabilize..."
sleep 30

# Deploy application services
echo "🔧 Deploying application services..."
./deploy-application.sh

echo ""
echo "✅ Complete deployment finished successfully!"
echo ""
echo "📊 Full Deployment Status:"
kubectl get pods -n fourcolour
echo ""
echo "🌐 All Services:"
kubectl get services -n fourcolour
echo ""
echo "🔗 To access the API Gateway:"
echo "   kubectl port-forward service/api-gateway-service 8080:8080 -n fourcolour"
echo ""
echo "📝 To view logs:"
echo "   kubectl logs -f deployment/api-gateway-service -n fourcolour" 