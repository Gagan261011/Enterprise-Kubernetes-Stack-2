#!/bin/bash
# Generated environment variables for the Enterprise Kubernetes Stack
# Source this file: source scripts/generated/env-vars.sh

export AWS_REGION="${aws_region}"
export AWS_ACCOUNT_ID="${aws_account_id}"
export ECR_REGISTRY="${ecr_registry}"
export MASTER_PUBLIC_IP="${master_public_ip}"
export MASTER_PRIVATE_IP="${master_private_ip}"
export PROJECT_NAME="${project_name}"

# ECR Repository URLs
%{ for name, url in ecr_repositories ~}
export ECR_REPO_${upper(replace(name, "-", "_"))}="${url}"
%{ endfor ~}

echo "Environment variables loaded successfully!"
echo "ECR Registry: $ECR_REGISTRY"
echo "Master Node: $MASTER_PUBLIC_IP"
