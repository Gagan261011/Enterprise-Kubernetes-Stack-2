# Enterprise Kubernetes Stack

A fully automated enterprise demo stack for an online shopping website, deployed on a self-managed Kubernetes cluster on AWS using **Terraform + Argo CD + AWS ECR**.

## 🏗️ Architecture Overview

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                                    AWS Cloud                                      │
├──────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────────────┐ │
│  │                              VPC (10.0.0.0/16)                               │ │
│  │  ┌─────────────────────────────────────────────────────────────────────────┐│ │
│  │  │                         Kubernetes Cluster                              ││ │
│  │  │                                                                         ││ │
│  │  │  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                 ││ │
│  │  │  │   Master    │    │  Worker 1   │    │  Worker 2   │    Worker 3     ││ │
│  │  │  │  (Control)  │    │             │    │             │                 ││ │
│  │  │  └─────────────┘    └─────────────┘    └─────────────┘                 ││ │
│  │  │                                                                         ││ │
│  │  │  ┌──────────────────────────────────────────────────────────────────┐  ││ │
│  │  │  │                    Namespace: frontend                           │  ││ │
│  │  │  │  ┌─────────────────────────────────────────────────────────────┐ │  ││ │
│  │  │  │  │  Next.js Frontend (React + Tailwind CSS)                    │ │  ││ │
│  │  │  │  │  - Product Catalog  - Shopping Cart  - Order Management     │ │  ││ │
│  │  │  │  └─────────────────────────────────────────────────────────────┘ │  ││ │
│  │  │  └──────────────────────────────────────────────────────────────────┘  ││ │
│  │  │                                   │ HTTP                                ││ │
│  │  │                                   ▼                                     ││ │
│  │  │  ┌──────────────────────────────────────────────────────────────────┐  ││ │
│  │  │  │                    Namespace: middleware                         │  ││ │
│  │  │  │  ┌─────────────────┐              ┌─────────────────┐           │  ││ │
│  │  │  │  │    User BFF     │              │    Order BFF    │           │  ││ │
│  │  │  │  │  (Spring Boot)  │              │  (Spring Boot)  │           │  ││ │
│  │  │  │  └────────┬────────┘              └────────┬────────┘           │  ││ │
│  │  │  │           │         mTLS                   │                     │  ││ │
│  │  │  │           └────────────┬───────────────────┘                     │  ││ │
│  │  │  │                        ▼                                         │  ││ │
│  │  │  │  ┌─────────────────────────────────────────────────────────────┐ │  ││ │
│  │  │  │  │              Security Middleware (mTLS Gateway)             │ │  ││ │
│  │  │  │  │        - Certificate Validation  - Request Proxying         │ │  ││ │
│  │  │  │  └─────────────────────────────────────────────────────────────┘ │  ││ │
│  │  │  └──────────────────────────────────────────────────────────────────┘  ││ │
│  │  │                                   │ HTTP                                ││ │
│  │  │                                   ▼                                     ││ │
│  │  │  ┌──────────────────────────────────────────────────────────────────┐  ││ │
│  │  │  │                    Namespace: backend                            │  ││ │
│  │  │  │  ┌─────────────────────────────────────────────────────────────┐ │  ││ │
│  │  │  │  │              Backend Service (Spring Boot)                  │ │  ││ │
│  │  │  │  │  REST API │ GraphQL │ SOAP │ H2 Database                    │ │  ││ │
│  │  │  │  └─────────────────────────────────────────────────────────────┘ │  ││ │
│  │  │  └──────────────────────────────────────────────────────────────────┘  ││ │
│  │  └─────────────────────────────────────────────────────────────────────────┘│ │
│  └─────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                   │
│  ┌─────────────────┐                                                             │
│  │    AWS ECR      │  ← Container Registry for all images                        │
│  │  (5 repos)      │                                                             │
│  └─────────────────┘                                                             │
└──────────────────────────────────────────────────────────────────────────────────┘
```

## 🚀 Features

### Infrastructure
- **Terraform-managed AWS infrastructure** (VPC, EC2, Security Groups, ECR)
- **Self-managed Kubernetes cluster** using kubeadm with Calico CNI
- **GitOps with Argo CD** for continuous deployment
- **AWS ECR** for private container registry

### Security
- **mTLS (Mutual TLS)** between all services
- **Self-signed CA** with service-specific certificates
- **Security middleware** for certificate validation
- **Namespace isolation** for multi-tier architecture

### Application Stack
| Layer | Technology | Features |
|-------|------------|----------|
| **Frontend** | Next.js 14, React, TypeScript, Tailwind CSS | Product catalog, Cart, Orders, User auth |
| **User BFF** | Spring Boot 3.2 | User management, Product queries |
| **Order BFF** | Spring Boot 3.2 | Cart operations, Order processing |
| **Security Middleware** | Spring Boot 3.2 | mTLS gateway, Request proxying |
| **Backend Service** | Spring Boot 3.2 | REST, GraphQL, SOAP, H2 database |

### Multi-Protocol Support
- **REST API** - Primary API interface
- **GraphQL** - Flexible data queries
- **SOAP** - Legacy system integration

## 📋 Prerequisites

- AWS Account with appropriate permissions
- AWS CLI configured with credentials
- Terraform >= 1.0
- Docker (for building images locally)
- kubectl
- Git

## 🛠️ Quick Start

### Step 1: Clone and Configure

```bash
# Clone the repository
git clone <your-repo-url>
cd Enterprise-Kubernetes-Stack-2

# Copy and edit terraform variables
cp infra/terraform/terraform.tfvars.example infra/terraform/terraform.tfvars
# Edit terraform.tfvars with your values
```

### Step 2: Deploy Infrastructure

```bash
cd infra/terraform

# Initialize Terraform
terraform init

# Review the plan
terraform plan

# Apply (creates VPC, EC2 instances, ECR, Security Groups)
terraform apply
```

This creates:
- 1 VPC with public subnets
- 4 EC2 instances (1 master, 3 workers)
- 5 ECR repositories
- Security groups for K8s and SSH
- SSH key pair for access

### Step 3: Set Up Kubernetes Cluster

```bash
# SSH into master node
ssh -i k8s-cluster-key.pem ubuntu@<master-public-ip>

# Run the setup script (already copied by Terraform)
chmod +x /home/ubuntu/setup-k8s.sh
sudo /home/ubuntu/setup-k8s.sh master

# Get the join command and run on each worker node
```

### Step 4: Generate PKI Certificates

```bash
# On your local machine or master node
cd scripts
chmod +x generate-pki.sh
./generate-pki.sh

# This creates:
# - CA certificate and key
# - Service certificates for: backend, middleware, user-bff, order-bff
```

### Step 5: Build and Push Images

```bash
# Set environment variables
export AWS_REGION=ap-south-1
export ECR_REGISTRY=<your-account-id>.dkr.ecr.ap-south-1.amazonaws.com

# Build and push all images
chmod +x scripts/build-and-push-all.sh
./scripts/build-and-push-all.sh
```

### Step 6: Deploy Applications with Argo CD

```bash
# Install Argo CD
chmod +x scripts/setup-argocd.sh
./scripts/setup-argocd.sh

# Access Argo CD UI
# URL will be displayed after script completes

# Apply the main application (triggers GitOps sync)
kubectl apply -f argocd/application.yaml
```

### Step 7: Access the Application

```bash
# Get the frontend NodePort
kubectl get svc frontend -n frontend

# Access at http://<any-worker-node-ip>:30080
```

## 📁 Project Structure

```
Enterprise-Kubernetes-Stack-2/
├── infra/
│   └── terraform/
│       ├── main.tf                 # Main Terraform config
│       ├── variables.tf            # Variable definitions
│       ├── outputs.tf              # Output values
│       ├── terraform.tfvars.example
│       ├── modules/
│       │   ├── vpc/                # VPC module
│       │   ├── security-groups/    # Security groups module
│       │   ├── ec2/                # EC2 instances module
│       │   └── ecr/                # ECR repositories module
│       └── templates/
│           └── user_data.sh.tpl    # EC2 user data template
├── scripts/
│   ├── generate-pki.sh             # Generate mTLS certificates
│   ├── setup-k8s.sh                # Kubernetes cluster setup
│   ├── setup-argocd.sh             # Argo CD installation
│   └── build-and-push-all.sh       # Build and push all images
├── services/
│   ├── backend-service/            # Core business logic (Spring Boot)
│   │   ├── src/main/java/...
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── application.yaml
│   ├── security-middleware/        # mTLS Gateway (Spring Boot)
│   ├── user-bff/                   # User BFF (Spring Boot)
│   └── order-bff/                  # Order BFF (Spring Boot)
├── frontend/                       # Next.js Frontend
│   ├── app/
│   ├── components/
│   ├── lib/
│   ├── package.json
│   └── Dockerfile
├── k8s/
│   ├── kustomization.yaml
│   ├── base/
│   │   ├── namespaces.yaml
│   │   └── secrets-template.yaml
│   ├── backend/
│   ├── middleware/
│   └── frontend/
└── argocd/
    ├── project.yaml
    ├── application.yaml
    └── apps/
```

## 🔐 Security Architecture

### mTLS Flow

```
Frontend → User/Order BFF → Security Middleware → Backend Service
              │                    │
              │                    ├─ Validates client certificate
              │                    ├─ Checks CN against allowed list
              │                    └─ Proxies request to backend
              │
              └─ Presents client certificate (mTLS)
```

### Certificate Hierarchy

```
CA (Self-signed)
├── backend-service.crt
├── security-middleware.crt
├── user-bff.crt
└── order-bff.crt
```

## 🌐 API Endpoints

### REST Endpoints (via BFF)

| Service | Endpoint | Description |
|---------|----------|-------------|
| User BFF | `POST /api/users/register` | User registration |
| User BFF | `POST /api/users/login` | User login |
| User BFF | `GET /api/products` | List all products |
| User BFF | `GET /api/products/{id}` | Get product details |
| Order BFF | `GET /api/cart/{userId}` | Get user's cart |
| Order BFF | `POST /api/cart/{userId}/add` | Add item to cart |
| Order BFF | `POST /api/orders` | Create new order |
| Order BFF | `GET /api/orders/user/{userId}` | Get user's orders |

### GraphQL (Backend Service)

```graphql
# Queries
query {
  users { id, email, firstName, lastName }
  products { id, name, price, stock }
  orders(userId: 1) { id, status, totalAmount }
}

# Mutations
mutation {
  createUser(input: { email: "...", password: "...", firstName: "..." }) { id }
  createProduct(input: { name: "...", price: 99.99 }) { id }
}
```

### SOAP (Backend Service)

WSDL available at: `http://backend-service:8080/ws/users.wsdl`

## 📊 Monitoring & Health Checks

All services expose Spring Boot Actuator endpoints:

- `/actuator/health` - Health status
- `/actuator/health/liveness` - Kubernetes liveness probe
- `/actuator/health/readiness` - Kubernetes readiness probe
- `/actuator/info` - Application info
- `/actuator/metrics` - Metrics data

## 🔄 GitOps Workflow

1. **Developer pushes code** to Git repository
2. **Argo CD detects changes** and syncs with cluster
3. **New images are pulled** from ECR
4. **Rolling update** is performed automatically
5. **Health checks** verify successful deployment

## 🧪 Local Development

### Backend Services

```bash
cd services/backend-service
./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
# Access at http://localhost:3000
```

## 🧹 Cleanup

```bash
# Destroy all AWS resources
cd infra/terraform
terraform destroy

# This removes:
# - All EC2 instances
# - VPC and networking
# - ECR repositories (and images)
# - Security groups
# - SSH key pair
```

## 📝 Environment Variables

### Terraform Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `aws_region` | AWS region | `ap-south-1` |
| `environment` | Environment name | `demo` |
| `vpc_cidr` | VPC CIDR block | `10.0.0.0/16` |
| `instance_type` | EC2 instance type | `t3.medium` |
| `ssh_allowed_cidr` | CIDR for SSH access | `0.0.0.0/0` |

### Service Environment Variables

| Variable | Service | Description |
|----------|---------|-------------|
| `MIDDLEWARE_URL` | BFF services | Security middleware URL |
| `BACKEND_SERVICE_URL` | Middleware | Backend service URL |
| `NEXT_PUBLIC_USER_BFF_URL` | Frontend | User BFF URL |
| `NEXT_PUBLIC_ORDER_BFF_URL` | Frontend | Order BFF URL |

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Kubernetes community
- Argo CD project
- Terraform by HashiCorp
