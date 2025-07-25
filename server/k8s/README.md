# Four Colour Theorem Solver - Kubernetes Deployment

This directory contains all the necessary Kubernetes YAML files and scripts to deploy the Four Colour Theorem Solver microservices to a Kubernetes cluster.

## Prerequisites

Before deploying, ensure you have the following installed:

1. **Docker** - For building and pushing images
2. **kubectl** - Kubernetes command-line tool
3. **minikube** - For local Kubernetes cluster (optional, for local testing)
4. **Docker Hub account** - For pushing images

## Quick Start

### 1. Set up Local Kubernetes Cluster (minikube)

```bash
# Start minikube
minikube start

# Enable ingress addon (optional)
minikube addons enable ingress

# Verify cluster is running
kubectl cluster-info
```

### 2. Login to Docker Hub

```bash
docker login -u aqtran
```

### 3. Build and Push Docker Images

```bash
# Make scripts executable
chmod +x build-and-push.sh deploy.sh cleanup.sh

# Build and push all microservices to Docker Hub
./build-and-push.sh
```

### 4. Deploy to Kubernetes

```bash
# Deploy all services
./deploy.sh
```

### 5. Access the Application

```bash
# Port forward the API Gateway
kubectl port-forward service/api-gateway-service 8080:8080 -n fourcolour
```

Then access the application at `http://localhost:8080`

## Manual Deployment Steps

If you prefer to deploy manually:

### 1. Create Namespace and ConfigMap

```bash
kubectl apply -f namespace.yaml
kubectl apply -f configmap.yaml
```

### 2. Deploy Infrastructure Services

```bash
# Deploy databases and message brokers
kubectl apply -f postgres.yaml
kubectl apply -f mongo.yaml
kubectl apply -f redis.yaml
kubectl apply -f kafka.yaml

# Wait for infrastructure to be ready
kubectl wait --for=condition=available --timeout=300s deployment/postgres -n fourcolour
kubectl wait --for=condition=available --timeout=300s deployment/mongo -n fourcolour
kubectl wait --for=condition=available --timeout=300s deployment/redis -n fourcolour
kubectl wait --for=condition=available --timeout=300s deployment/zookeeper -n fourcolour
kubectl wait --for=condition=available --timeout=300s deployment/kafka -n fourcolour
```

### 3. Deploy Application Services

```bash
# Deploy microservices
kubectl apply -f logger-service.yaml
kubectl apply -f authentication-service.yaml
kubectl apply -f map-storage-service.yaml
kubectl apply -f solver-service.yaml
kubectl apply -f api-gateway-service.yaml

# Wait for services to be ready
kubectl wait --for=condition=available --timeout=300s deployment/logger-service -n fourcolour
kubectl wait --for=condition=available --timeout=300s deployment/authentication-service -n fourcolour
kubectl wait --for=condition=available --timeout=300s deployment/map-storage-service -n fourcolour
kubectl wait --for=condition=available --timeout=300s deployment/solver-service -n fourcolour
kubectl wait --for=condition=available --timeout=300s deployment/api-gateway-service -n fourcolour
```

## Monitoring and Troubleshooting

### Check Deployment Status

```bash
# View all pods
kubectl get pods -n fourcolour

# View services
kubectl get services -n fourcolour

# View deployments
kubectl get deployments -n fourcolour
```

### View Logs

```bash
# View logs for a specific service
kubectl logs -f deployment/api-gateway-service -n fourcolour
kubectl logs -f deployment/authentication-service -n fourcolour
kubectl logs -f deployment/solver-service -n fourcolour
```

### Check Service Health

```bash
# Check if services are responding
kubectl port-forward service/api-gateway-service 8080:8080 -n fourcolour &
curl http://localhost:8080/actuator/health
```

### Debug Pod Issues

```bash
# Describe a pod to see events
kubectl describe pod <pod-name> -n fourcolour

# Execute commands in a pod
kubectl exec -it <pod-name> -n fourcolour -- /bin/bash
```

## Cleanup

To remove all resources:

```bash
# Use the cleanup script
./cleanup.sh

# Or manually delete everything
kubectl delete namespace fourcolour
```

## Architecture Overview

The deployment consists of:

### Infrastructure Services

- **PostgreSQL** - User authentication and session storage
- **MongoDB** - Map storage and logging
- **Redis** - Caching and session management
- **Kafka & Zookeeper** - Message queuing for logging

### Application Services

- **API Gateway Service** - Main entry point, handles routing and authentication
- **Authentication Service** - User registration, login, and JWT token management
- **Map Storage Service** - Stores and retrieves map data
- **Solver Service** - Python service that implements the four color theorem algorithm
- **Logger Service** - Centralized logging with gRPC interface

### Service Dependencies

```
API Gateway → Authentication Service
API Gateway → Map Storage Service
API Gateway → Solver Service
API Gateway → Redis

Authentication Service → PostgreSQL
Authentication Service → Logger Service

Map Storage Service → MongoDB
Map Storage Service → Logger Service

Solver Service → Logger Service

Logger Service → MongoDB
Logger Service → Kafka
```

## Configuration

All configuration is managed through the `configmap.yaml` file. Key configurations include:

- Database connection strings
- Service URLs
- JWT secrets
- Kafka and Redis settings

## Scaling

To scale services:

```bash
# Scale API Gateway to 3 replicas
kubectl scale deployment api-gateway-service --replicas=3 -n fourcolour

# Scale Solver Service to 2 replicas
kubectl scale deployment solver-service --replicas=2 -n fourcolour
```

## Production Considerations

For production deployment:

1. **Use proper secrets management** instead of ConfigMaps for sensitive data
2. **Enable resource limits** in the deployment YAML files
3. **Set up proper ingress** for external access
4. **Configure persistent volumes** with appropriate storage classes
5. **Set up monitoring and alerting** (Prometheus, Grafana)
6. **Use a proper container registry** with image scanning
7. **Implement proper backup strategies** for databases

## Troubleshooting Common Issues

### Pods stuck in Pending state

- Check if there are enough resources in the cluster
- Verify that the Docker images exist and are accessible

### Services not communicating

- Check if all services are in the same namespace
- Verify service names and ports in the ConfigMap
- Check if databases are ready before deploying application services

### Database connection issues

- Ensure databases are fully started before deploying application services
- Check the connection strings in the ConfigMap
- Verify that the database credentials are correct

### Image pull errors

- Ensure you're logged into Docker Hub
- Check that the images were pushed successfully
- Verify the image names and tags in the deployment files
