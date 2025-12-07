#!/bin/bash
# Argo CD Installation and Setup Script

set -e

ARGOCD_VERSION="v2.9.3"
ARGOCD_NAMESPACE="argocd"

echo "=========================================="
echo "Installing Argo CD ${ARGOCD_VERSION}"
echo "=========================================="

# Create namespace
kubectl create namespace ${ARGOCD_NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -

# Install Argo CD
kubectl apply -n ${ARGOCD_NAMESPACE} -f https://raw.githubusercontent.com/argoproj/argo-cd/${ARGOCD_VERSION}/manifests/install.yaml

echo "Waiting for Argo CD pods to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/argocd-server -n ${ARGOCD_NAMESPACE}

# Get initial admin password
echo ""
echo "=========================================="
echo "Argo CD Installation Complete!"
echo "=========================================="
echo ""
echo "Initial admin password:"
kubectl -n ${ARGOCD_NAMESPACE} get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
echo ""
echo ""

# Patch to allow insecure (for demo purposes)
kubectl patch svc argocd-server -n ${ARGOCD_NAMESPACE} -p '{"spec": {"type": "NodePort"}}'

# Get NodePort
ARGOCD_PORT=$(kubectl get svc argocd-server -n ${ARGOCD_NAMESPACE} -o jsonpath='{.spec.ports[?(@.name=="https")].nodePort}')
echo "Argo CD UI available at: https://<MASTER_NODE_IP>:${ARGOCD_PORT}"
echo ""
echo "Login with:"
echo "  Username: admin"
echo "  Password: (shown above)"
echo ""

# Apply project and applications
echo "Applying Argo CD Project and Applications..."
kubectl apply -f argocd/project.yaml
kubectl apply -f argocd/application.yaml

echo ""
echo "Argo CD setup complete!"
echo "Applications will now sync automatically from Git."
