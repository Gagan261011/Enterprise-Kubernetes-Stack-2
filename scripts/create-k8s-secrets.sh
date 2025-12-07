#!/bin/bash
# Script to create Kubernetes secrets from PKI certificates

set -e

PKI_DIR="${1:-./pki}"
KEYSTORE_PASSWORD="${2:-changeit}"

echo "=========================================="
echo "Creating Kubernetes Secrets from PKI"
echo "=========================================="

if [ ! -d "$PKI_DIR" ]; then
    echo "ERROR: PKI directory not found: $PKI_DIR"
    echo "Please run generate-pki.sh first"
    exit 1
fi

# Create namespaces if they don't exist
kubectl create namespace backend --dry-run=client -o yaml | kubectl apply -f -
kubectl create namespace middleware --dry-run=client -o yaml | kubectl apply -f -
kubectl create namespace frontend --dry-run=client -o yaml | kubectl apply -f -

echo ""
echo "Creating secrets for backend namespace..."
kubectl create secret generic mtls-certs \
    --namespace=backend \
    --from-file=ca.crt=${PKI_DIR}/ca.crt \
    --from-file=server.crt=${PKI_DIR}/backend-service.crt \
    --from-file=server.key=${PKI_DIR}/backend-service.key \
    --dry-run=client -o yaml | kubectl apply -f -

echo ""
echo "Creating secrets for middleware namespace..."

# Security middleware
kubectl create secret generic mtls-certs \
    --namespace=middleware \
    --from-file=ca.crt=${PKI_DIR}/ca.crt \
    --from-file=keystore.p12=${PKI_DIR}/security-middleware.p12 \
    --from-file=truststore.p12=${PKI_DIR}/truststore.p12 \
    --dry-run=client -o yaml | kubectl apply -f -

# User BFF
kubectl create secret generic mtls-certs-user-bff \
    --namespace=middleware \
    --from-file=ca.crt=${PKI_DIR}/ca.crt \
    --from-file=keystore.p12=${PKI_DIR}/user-bff.p12 \
    --from-file=truststore.p12=${PKI_DIR}/truststore.p12 \
    --dry-run=client -o yaml | kubectl apply -f -

# Order BFF
kubectl create secret generic mtls-certs-order-bff \
    --namespace=middleware \
    --from-file=ca.crt=${PKI_DIR}/ca.crt \
    --from-file=keystore.p12=${PKI_DIR}/order-bff.p12 \
    --from-file=truststore.p12=${PKI_DIR}/truststore.p12 \
    --dry-run=client -o yaml | kubectl apply -f -

echo ""
echo "=========================================="
echo "Secrets created successfully!"
echo "=========================================="
echo ""
echo "Secrets in backend namespace:"
kubectl get secrets -n backend
echo ""
echo "Secrets in middleware namespace:"
kubectl get secrets -n middleware
