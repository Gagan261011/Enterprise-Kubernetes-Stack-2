#!/bin/bash
# PKI Certificate Generation Script
# Generates a self-signed CA and service certificates for mTLS

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PKI_DIR="${SCRIPT_DIR}/../pki"
CERTS_DIR="${PKI_DIR}/certs"
KEYS_DIR="${PKI_DIR}/keys"

# Configuration
CA_DAYS=3650
CERT_DAYS=365
KEY_SIZE=2048

# Service names
SERVICES=("user-bff" "order-bff" "security-middleware" "backend-service")

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Create directories
mkdir -p "${CERTS_DIR}" "${KEYS_DIR}"

# Generate Root CA
generate_ca() {
    log_info "Generating Root CA..."
    
    # Generate CA private key
    openssl genrsa -out "${KEYS_DIR}/ca.key" ${KEY_SIZE}
    
    # Generate CA certificate
    openssl req -x509 -new -nodes \
        -key "${KEYS_DIR}/ca.key" \
        -sha256 \
        -days ${CA_DAYS} \
        -out "${CERTS_DIR}/ca.crt" \
        -subj "/C=US/ST=California/L=San Francisco/O=Enterprise K8s Shop/OU=Platform/CN=Enterprise K8s Shop CA"
    
    log_info "Root CA generated successfully"
}

# Generate service certificate
generate_service_cert() {
    local service_name=$1
    log_info "Generating certificate for ${service_name}..."
    
    # Create CSR config
    cat > "${PKI_DIR}/${service_name}.cnf" <<EOF
[req]
default_bits = ${KEY_SIZE}
prompt = no
default_md = sha256
distinguished_name = dn
req_extensions = req_ext

[dn]
C = US
ST = California
L = San Francisco
O = Enterprise K8s Shop
OU = ${service_name}
CN = ${service_name}

[req_ext]
subjectAltName = @alt_names

[alt_names]
DNS.1 = ${service_name}
DNS.2 = ${service_name}.middleware
DNS.3 = ${service_name}.middleware.svc
DNS.4 = ${service_name}.middleware.svc.cluster.local
DNS.5 = ${service_name}.backend
DNS.6 = ${service_name}.backend.svc
DNS.7 = ${service_name}.backend.svc.cluster.local
DNS.8 = localhost
IP.1 = 127.0.0.1
EOF

    # Generate private key
    openssl genrsa -out "${KEYS_DIR}/${service_name}.key" ${KEY_SIZE}
    
    # Generate CSR
    openssl req -new \
        -key "${KEYS_DIR}/${service_name}.key" \
        -out "${PKI_DIR}/${service_name}.csr" \
        -config "${PKI_DIR}/${service_name}.cnf"
    
    # Create extension file for SAN
    cat > "${PKI_DIR}/${service_name}.ext" <<EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth, clientAuth
subjectAltName = @alt_names

[alt_names]
DNS.1 = ${service_name}
DNS.2 = ${service_name}.middleware
DNS.3 = ${service_name}.middleware.svc
DNS.4 = ${service_name}.middleware.svc.cluster.local
DNS.5 = ${service_name}.backend
DNS.6 = ${service_name}.backend.svc
DNS.7 = ${service_name}.backend.svc.cluster.local
DNS.8 = localhost
IP.1 = 127.0.0.1
EOF

    # Sign certificate with CA
    openssl x509 -req \
        -in "${PKI_DIR}/${service_name}.csr" \
        -CA "${CERTS_DIR}/ca.crt" \
        -CAkey "${KEYS_DIR}/ca.key" \
        -CAcreateserial \
        -out "${CERTS_DIR}/${service_name}.crt" \
        -days ${CERT_DAYS} \
        -sha256 \
        -extfile "${PKI_DIR}/${service_name}.ext"
    
    # Cleanup CSR and config files
    rm -f "${PKI_DIR}/${service_name}.csr" "${PKI_DIR}/${service_name}.cnf" "${PKI_DIR}/${service_name}.ext"
    
    log_info "Certificate for ${service_name} generated successfully"
}

# Generate Kubernetes secrets YAML
generate_k8s_secrets() {
    log_info "Generating Kubernetes secrets YAML files..."
    
    local SECRETS_DIR="${SCRIPT_DIR}/../k8s/secrets"
    mkdir -p "${SECRETS_DIR}"
    
    # CA Secret (for all namespaces)
    for ns in middleware backend; do
        cat > "${SECRETS_DIR}/ca-secret-${ns}.yaml" <<EOF
apiVersion: v1
kind: Secret
metadata:
  name: ca-certificate
  namespace: ${ns}
type: Opaque
data:
  ca.crt: $(base64 -w 0 "${CERTS_DIR}/ca.crt")
EOF
    done
    
    # Service secrets
    for service in "${SERVICES[@]}"; do
        local namespace="middleware"
        if [ "$service" == "backend-service" ]; then
            namespace="backend"
        fi
        
        cat > "${SECRETS_DIR}/${service}-tls-secret.yaml" <<EOF
apiVersion: v1
kind: Secret
metadata:
  name: ${service}-tls
  namespace: ${namespace}
type: kubernetes.io/tls
data:
  tls.crt: $(base64 -w 0 "${CERTS_DIR}/${service}.crt")
  tls.key: $(base64 -w 0 "${KEYS_DIR}/${service}.key")
  ca.crt: $(base64 -w 0 "${CERTS_DIR}/ca.crt")
EOF
    done
    
    log_info "Kubernetes secrets generated in ${SECRETS_DIR}"
}

# Main execution
main() {
    log_info "Starting PKI generation..."
    
    # Check if openssl is available
    if ! command -v openssl &> /dev/null; then
        log_error "OpenSSL is required but not installed"
        exit 1
    fi
    
    # Generate CA
    generate_ca
    
    # Generate service certificates
    for service in "${SERVICES[@]}"; do
        generate_service_cert "$service"
    done
    
    # Generate Kubernetes secrets
    generate_k8s_secrets
    
    log_info "PKI generation completed successfully!"
    log_info "Certificates are in: ${CERTS_DIR}"
    log_info "Private keys are in: ${KEYS_DIR}"
    
    echo ""
    log_info "Generated files:"
    ls -la "${CERTS_DIR}"
    echo ""
    ls -la "${KEYS_DIR}"
}

main "$@"
