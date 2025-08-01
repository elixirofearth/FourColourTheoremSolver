#!/bin/bash

# Setup HTTPS for Four Colour Theorem Solver using nip.io
# 
# This script uses nip.io which automatically resolves domains to IP addresses
# No domain registration or DNS configuration needed!

set -e

# Get the cluster IP automatically
CLUSTER_IP="149.248.46.187"
DOMAIN="api.${CLUSTER_IP}.nip.io"
EMAIL="anhquoctran006@gmail.com"  # UPDATE THIS with your email

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Setting up HTTPS using nip.io${NC}"
echo -e "${BLUE}=================================${NC}"

# Check if email is configured
if [[ "$EMAIL" == "your-email@example.com" ]]; then
    echo -e "${RED}❌ Please update EMAIL variable in this script first!${NC}"
    echo -e "${YELLOW}Edit this file and set EMAIL to your email address${NC}"
    exit 1
fi

echo -e "${YELLOW}📋 Configuration:${NC}"
echo -e "  Cluster IP: ${CLUSTER_IP}"
echo -e "  Domain: ${DOMAIN}"
echo -e "  Email: ${EMAIL}"
echo ""

# Step 1: Install cert-manager
echo -e "${BLUE}📦 Installing cert-manager...${NC}"
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.3/cert-manager.yaml

echo -e "${YELLOW}⏳ Waiting for cert-manager to be ready (this may take 2-3 minutes)...${NC}"
kubectl wait --for=condition=ready pod -l app=cert-manager -n cert-manager --timeout=300s
kubectl wait --for=condition=ready pod -l app=cainjector -n cert-manager --timeout=300s
kubectl wait --for=condition=ready pod -l app=webhook -n cert-manager --timeout=300s

# Step 2: Create ClusterIssuer
echo -e "${BLUE}🔐 Creating Let's Encrypt ClusterIssuer...${NC}"
cat <<EOF | kubectl apply -f -
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: ${EMAIL}
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
EOF

# Step 3: Create HTTPS ingress with nip.io domain
echo -e "${BLUE}🌐 Creating HTTPS ingress with nip.io...${NC}"
cat <<EOF | kubectl apply -f -
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: fourcolour-ingress-https
  namespace: fourcolour
  annotations:
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
    nginx.ingress.kubernetes.io/enable-cors: "true"
    nginx.ingress.kubernetes.io/cors-allow-origin: "https://four-colour-theorem-solver.vercel.app"
    nginx.ingress.kubernetes.io/cors-allow-methods: "GET, POST, PUT, DELETE, OPTIONS"
    nginx.ingress.kubernetes.io/cors-allow-headers: "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization"
    nginx.ingress.kubernetes.io/cors-allow-credentials: "true"
    nginx.ingress.kubernetes.io/proxy-connect-timeout: "600"
    nginx.ingress.kubernetes.io/proxy-send-timeout: "600"
    nginx.ingress.kubernetes.io/proxy-read-timeout: "600"
    nginx.ingress.kubernetes.io/proxy-body-size: "10m"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  ingressClassName: nginx
  tls:
    - hosts:
        - ${DOMAIN}
      secretName: fourcolour-tls-secret
  rules:
    - host: ${DOMAIN}
      http:
        paths:
          - path: /api
            pathType: Prefix
            backend:
              service:
                name: api-gateway-service
                port:
                  number: 8080
          - path: /actuator
            pathType: Prefix
            backend:
              service:
                name: api-gateway-service
                port:
                  number: 8080
          - path: /
            pathType: Prefix
            backend:
              service:
                name: api-gateway-service
                port:
                  number: 8080
EOF

echo -e "${GREEN}✅ HTTPS setup with nip.io complete!${NC}"
echo -e "${YELLOW}📋 Your API is now available at:${NC}"
echo -e "   ${BLUE}https://${DOMAIN}${NC}"
echo ""
echo -e "${YELLOW}📋 Next steps:${NC}"
echo -e "1. Wait 2-3 minutes for certificate to be issued"
echo -e "2. Check certificate status: ${BLUE}kubectl get certificate -n fourcolour${NC}"
echo -e "3. Test your API: ${BLUE}curl https://${DOMAIN}/actuator/health${NC}"
echo -e "4. Update your frontend to use: ${BLUE}https://${DOMAIN}${NC}"

echo ""
echo -e "${YELLOW}🔍 Monitor certificate creation:${NC}"
echo -e "${BLUE}kubectl describe certificate fourcolour-tls-secret -n fourcolour${NC}"
echo -e "${BLUE}kubectl get certificaterequests -n fourcolour${NC}"

echo ""
echo -e "${GREEN}🎉 Your HTTPS API URL: https://${DOMAIN}${NC}" 