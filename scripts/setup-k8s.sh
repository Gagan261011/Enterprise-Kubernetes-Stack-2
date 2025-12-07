#!/bin/bash
# Kubernetes Cluster Setup Script
# Initializes kubeadm master and joins workers

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GENERATED_DIR="${SCRIPT_DIR}/generated"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Source environment variables
if [ -f "${GENERATED_DIR}/env-vars.sh" ]; then
    source "${GENERATED_DIR}/env-vars.sh"
else
    log_error "Environment variables file not found. Run terraform apply first."
    exit 1
fi

# Parse inventory file
parse_inventory() {
    log_info "Parsing inventory file..."
    
    MASTER_IP=$(grep "master-1" "${GENERATED_DIR}/inventory.ini" | awk '{print $2}' | cut -d'=' -f2)
    MASTER_PRIVATE_IP=$(grep "master-1" "${GENERATED_DIR}/inventory.ini" | awk '{print $4}' | cut -d'=' -f2)
    SSH_KEY=$(grep "ansible_ssh_private_key_file" "${GENERATED_DIR}/inventory.ini" | cut -d'=' -f2)
    
    WORKER_IPS=()
    while IFS= read -r line; do
        if [[ $line =~ worker-[0-9]+ ]]; then
            ip=$(echo "$line" | awk '{print $2}' | cut -d'=' -f2)
            WORKER_IPS+=("$ip")
        fi
    done < "${GENERATED_DIR}/inventory.ini"
    
    log_info "Master IP: ${MASTER_IP}"
    log_info "Worker IPs: ${WORKER_IPS[*]}"
}

# Wait for node to be ready
wait_for_node() {
    local ip=$1
    local max_attempts=30
    local attempt=1
    
    log_info "Waiting for node ${ip} to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if ssh -o StrictHostKeyChecking=no -o ConnectTimeout=5 -i "${SSH_KEY}" ubuntu@"${ip}" "test -f /tmp/k8s-node-init-complete" 2>/dev/null; then
            log_info "Node ${ip} is ready"
            return 0
        fi
        log_warn "Attempt ${attempt}/${max_attempts}: Node ${ip} not ready yet..."
        sleep 10
        ((attempt++))
    done
    
    log_error "Node ${ip} did not become ready in time"
    return 1
}

# Initialize master node
init_master() {
    log_info "Initializing Kubernetes master node..."
    
    wait_for_node "${MASTER_IP}"
    
    # Copy kubeadm config to master
    scp -o StrictHostKeyChecking=no -i "${SSH_KEY}" \
        "${GENERATED_DIR}/kubeadm-config.yaml" \
        ubuntu@"${MASTER_IP}":/tmp/kubeadm-config.yaml
    
    # Initialize cluster
    ssh -o StrictHostKeyChecking=no -i "${SSH_KEY}" ubuntu@"${MASTER_IP}" << 'MASTER_INIT'
set -e

# Initialize kubeadm
sudo kubeadm init --config /tmp/kubeadm-config.yaml --upload-certs

# Setup kubectl for ubuntu user
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

# Install Calico CNI
kubectl apply -f https://raw.githubusercontent.com/projectcalico/calico/v3.26.1/manifests/calico.yaml

# Wait for Calico to be ready
echo "Waiting for Calico to be ready..."
sleep 30
kubectl wait --for=condition=ready pod -l k8s-app=calico-node -n kube-system --timeout=300s

# Generate join command and save it
kubeadm token create --print-join-command > /tmp/kubeadm-join-command.sh
chmod +x /tmp/kubeadm-join-command.sh

echo "Master initialization complete!"
MASTER_INIT
    
    # Get join command
    scp -o StrictHostKeyChecking=no -i "${SSH_KEY}" \
        ubuntu@"${MASTER_IP}":/tmp/kubeadm-join-command.sh \
        "${GENERATED_DIR}/kubeadm-join-command.sh"
    
    # Get kubeconfig
    scp -o StrictHostKeyChecking=no -i "${SSH_KEY}" \
        ubuntu@"${MASTER_IP}":.kube/config \
        "${GENERATED_DIR}/kubeconfig"
    
    log_info "Master node initialized successfully"
}

# Join worker nodes
join_workers() {
    log_info "Joining worker nodes to the cluster..."
    
    JOIN_COMMAND=$(cat "${GENERATED_DIR}/kubeadm-join-command.sh")
    
    for worker_ip in "${WORKER_IPS[@]}"; do
        log_info "Joining worker ${worker_ip}..."
        
        wait_for_node "${worker_ip}"
        
        ssh -o StrictHostKeyChecking=no -i "${SSH_KEY}" ubuntu@"${worker_ip}" << WORKER_JOIN
set -e
sudo ${JOIN_COMMAND}
echo "Worker node joined successfully!"
WORKER_JOIN
        
        log_info "Worker ${worker_ip} joined successfully"
    done
}

# Install Argo CD
install_argocd() {
    log_info "Installing Argo CD..."
    
    export KUBECONFIG="${GENERATED_DIR}/kubeconfig"
    
    # Create argocd namespace
    kubectl create namespace argocd --dry-run=client -o yaml | kubectl apply -f -
    
    # Install Argo CD
    kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
    
    # Wait for Argo CD to be ready
    log_info "Waiting for Argo CD to be ready..."
    kubectl wait --for=condition=available deployment/argocd-server -n argocd --timeout=300s
    
    # Patch service to use NodePort
    kubectl patch svc argocd-server -n argocd -p '{"spec": {"type": "NodePort", "ports": [{"name": "https", "port": 443, "targetPort": 8080, "nodePort": 30443}]}}'
    
    # Get initial admin password
    ARGOCD_PASSWORD=$(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d)
    
    log_info "Argo CD installed successfully!"
    log_info "Argo CD URL: https://${MASTER_IP}:30443"
    log_info "Username: admin"
    log_info "Password: ${ARGOCD_PASSWORD}"
    
    # Save credentials
    cat > "${GENERATED_DIR}/argocd-credentials.txt" << EOF
Argo CD Credentials
===================
URL: https://${MASTER_IP}:30443
Username: admin
Password: ${ARGOCD_PASSWORD}
EOF
    
    log_info "Credentials saved to ${GENERATED_DIR}/argocd-credentials.txt"
}

# Create namespaces
create_namespaces() {
    log_info "Creating Kubernetes namespaces..."
    
    export KUBECONFIG="${GENERATED_DIR}/kubeconfig"
    
    for ns in frontend middleware backend; do
        kubectl create namespace ${ns} --dry-run=client -o yaml | kubectl apply -f -
    done
    
    log_info "Namespaces created successfully"
}

# Setup ECR credentials on nodes
setup_ecr_credentials() {
    log_info "Setting up ECR credentials on all nodes..."
    
    # Get ECR login token
    ECR_TOKEN=$(aws ecr get-login-password --region ${AWS_REGION})
    
    # Create ECR registry secret
    export KUBECONFIG="${GENERATED_DIR}/kubeconfig"
    
    for ns in frontend middleware backend; do
        kubectl create secret docker-registry ecr-registry-secret \
            --docker-server=${ECR_REGISTRY} \
            --docker-username=AWS \
            --docker-password="${ECR_TOKEN}" \
            -n ${ns} \
            --dry-run=client -o yaml | kubectl apply -f -
    done
    
    log_info "ECR credentials configured in all namespaces"
}

# Main execution
main() {
    log_info "Starting Kubernetes cluster setup..."
    
    parse_inventory
    init_master
    join_workers
    create_namespaces
    install_argocd
    setup_ecr_credentials
    
    log_info "Kubernetes cluster setup completed successfully!"
    log_info ""
    log_info "Next steps:"
    log_info "1. Run ./scripts/generate-pki.sh to generate mTLS certificates"
    log_info "2. Run ./scripts/build-and-push-all.sh to build and push Docker images"
    log_info "3. Apply Kubernetes secrets: kubectl apply -f k8s/secrets/"
    log_info "4. Configure Argo CD to watch this repository"
    log_info ""
    log_info "Kubeconfig saved to: ${GENERATED_DIR}/kubeconfig"
    log_info "Use: export KUBECONFIG=${GENERATED_DIR}/kubeconfig"
}

main "$@"
