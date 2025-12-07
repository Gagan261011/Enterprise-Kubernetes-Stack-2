#!/bin/bash
# Build and Push All Docker Images to ECR
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="${SCRIPT_DIR}/.."
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

# Image tag (use git commit hash or 'latest')
IMAGE_TAG=${1:-latest}

# Services to build
declare -A SERVICES
SERVICES["frontend"]="${ROOT_DIR}/frontend"
SERVICES["user-bff"]="${ROOT_DIR}/services/user-bff"
SERVICES["order-bff"]="${ROOT_DIR}/services/order-bff"
SERVICES["security-middleware"]="${ROOT_DIR}/services/security-middleware"
SERVICES["backend-service"]="${ROOT_DIR}/services/backend-service"

# Login to ECR
ecr_login() {
    log_info "Logging into ECR..."
    aws ecr get-login-password --region ${AWS_REGION} | \
        docker login --username AWS --password-stdin ${ECR_REGISTRY}
    log_info "ECR login successful"
}

# Build and push a single service
build_and_push() {
    local service_name=$1
    local service_path=$2
    local image_uri="${ECR_REGISTRY}/${PROJECT_NAME}/${service_name}:${IMAGE_TAG}"
    
    log_info "Building ${service_name}..."
    
    if [ ! -f "${service_path}/Dockerfile" ]; then
        log_error "Dockerfile not found in ${service_path}"
        return 1
    fi
    
    # Build image
    docker build -t "${image_uri}" "${service_path}"
    
    # Also tag as latest if not already
    if [ "${IMAGE_TAG}" != "latest" ]; then
        docker tag "${image_uri}" "${ECR_REGISTRY}/${PROJECT_NAME}/${service_name}:latest"
    fi
    
    log_info "Pushing ${service_name} to ECR..."
    docker push "${image_uri}"
    
    if [ "${IMAGE_TAG}" != "latest" ]; then
        docker push "${ECR_REGISTRY}/${PROJECT_NAME}/${service_name}:latest"
    fi
    
    log_info "${service_name} built and pushed successfully"
}

# Update Kubernetes manifests with new image tags
update_manifests() {
    log_info "Updating Kubernetes manifests with image tags..."
    
    for service_name in "${!SERVICES[@]}"; do
        local image_uri="${ECR_REGISTRY}/${PROJECT_NAME}/${service_name}:${IMAGE_TAG}"
        
        # Update deployment manifests
        if [ "${service_name}" == "frontend" ]; then
            sed -i "s|image:.*${service_name}.*|image: ${image_uri}|g" \
                "${ROOT_DIR}/k8s/apps/frontend/"*.yaml 2>/dev/null || true
        elif [ "${service_name}" == "backend-service" ]; then
            sed -i "s|image:.*${service_name}.*|image: ${image_uri}|g" \
                "${ROOT_DIR}/k8s/apps/backend/"*.yaml 2>/dev/null || true
        else
            sed -i "s|image:.*${service_name}.*|image: ${image_uri}|g" \
                "${ROOT_DIR}/k8s/apps/middleware/"*.yaml 2>/dev/null || true
        fi
    done
    
    log_info "Manifests updated"
}

# Main execution
main() {
    log_info "Starting build and push for all services..."
    log_info "Image tag: ${IMAGE_TAG}"
    
    # Check for required tools
    if ! command -v docker &> /dev/null; then
        log_error "Docker is required but not installed"
        exit 1
    fi
    
    if ! command -v aws &> /dev/null; then
        log_error "AWS CLI is required but not installed"
        exit 1
    fi
    
    # Login to ECR
    ecr_login
    
    # Build and push each service
    for service_name in "${!SERVICES[@]}"; do
        build_and_push "${service_name}" "${SERVICES[$service_name]}"
    done
    
    # Update manifests
    update_manifests
    
    log_info ""
    log_info "All services built and pushed successfully!"
    log_info ""
    log_info "Images pushed:"
    for service_name in "${!SERVICES[@]}"; do
        echo "  - ${ECR_REGISTRY}/${PROJECT_NAME}/${service_name}:${IMAGE_TAG}"
    done
    
    log_info ""
    log_info "Next steps:"
    log_info "1. Commit and push the updated manifests to Git"
    log_info "2. Argo CD will automatically sync the changes"
}

main "$@"
