# Create ECR credentials secret for all namespaces
# This script should be run after ECR is set up

#!/bin/bash

set -e

AWS_REGION="${AWS_REGION:-ap-south-1}"
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

echo "Creating ECR credentials for all namespaces..."

# Get ECR login password
ECR_TOKEN=$(aws ecr get-login-password --region ${AWS_REGION})
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

# Create docker config JSON
DOCKER_CONFIG=$(echo -n "{\"auths\":{\"${ECR_REGISTRY}\":{\"username\":\"AWS\",\"password\":\"${ECR_TOKEN}\"}}}" | base64 -w0)

# Create secrets in each namespace
for NS in backend middleware frontend; do
    kubectl create namespace ${NS} --dry-run=client -o yaml | kubectl apply -f -
    
    kubectl create secret docker-registry ecr-credentials \
        --namespace=${NS} \
        --docker-server=${ECR_REGISTRY} \
        --docker-username=AWS \
        --docker-password=${ECR_TOKEN} \
        --dry-run=client -o yaml | kubectl apply -f -
    
    echo "Created ECR credentials in ${NS} namespace"
done

echo ""
echo "ECR credentials created in all namespaces"
echo "Note: ECR tokens expire after 12 hours. Consider setting up a CronJob for renewal."
