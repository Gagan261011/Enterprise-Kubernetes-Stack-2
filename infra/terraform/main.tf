# Enterprise Kubernetes Stack - Main Terraform Configuration
# AWS Infrastructure for Self-Managed Kubernetes Cluster

terraform {
  required_version = ">= 1.0.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    tls = {
      source  = "hashicorp/tls"
      version = "~> 4.0"
    }
    local = {
      source  = "hashicorp/local"
      version = "~> 2.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# Data source to get current AWS account ID
data "aws_caller_identity" "current" {}

# Data source for availability zones
data "aws_availability_zones" "available" {
  state = "available"
}

# Local variables
locals {
  cluster_name = "${var.project_name}-cluster"
  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "Terraform"
  }
}

# VPC Module
module "vpc" {
  source = "./modules/vpc"

  project_name       = var.project_name
  environment        = var.environment
  vpc_cidr           = var.vpc_cidr
  availability_zones = slice(data.aws_availability_zones.available.names, 0, 2)
  common_tags        = local.common_tags
}

# Security Groups Module
module "security_groups" {
  source = "./modules/security-groups"

  project_name   = var.project_name
  environment    = var.environment
  vpc_id         = module.vpc.vpc_id
  vpc_cidr       = var.vpc_cidr
  allowed_ssh_ip = var.allowed_ssh_ip
  common_tags    = local.common_tags
}

# EC2 Instances Module (K8s Nodes)
module "ec2_instances" {
  source = "./modules/ec2"

  project_name          = var.project_name
  environment           = var.environment
  instance_type         = var.instance_type
  key_name              = var.key_name
  master_count          = var.master_count
  worker_count          = var.worker_count
  subnet_ids            = module.vpc.public_subnet_ids
  master_security_group = module.security_groups.master_sg_id
  worker_security_group = module.security_groups.worker_sg_id
  common_tags           = local.common_tags
}

# ECR Repositories Module
module "ecr" {
  source = "./modules/ecr"

  project_name = var.project_name
  environment  = var.environment
  common_tags  = local.common_tags
  repositories = [
    "frontend",
    "user-bff",
    "order-bff",
    "security-middleware",
    "backend-service"
  ]
}

# Generate kubeadm configuration
resource "local_file" "kubeadm_config" {
  content = templatefile("${path.module}/templates/kubeadm-config.yaml.tpl", {
    master_private_ip = module.ec2_instances.master_private_ips[0]
    master_public_ip  = module.ec2_instances.master_public_ips[0]
    pod_network_cidr  = var.pod_network_cidr
    service_cidr      = var.service_cidr
    cluster_name      = local.cluster_name
  })
  filename = "${path.module}/../../scripts/generated/kubeadm-config.yaml"
}

# Generate inventory file for setup scripts
resource "local_file" "inventory" {
  content = templatefile("${path.module}/templates/inventory.tpl", {
    master_public_ips  = module.ec2_instances.master_public_ips
    master_private_ips = module.ec2_instances.master_private_ips
    worker_public_ips  = module.ec2_instances.worker_public_ips
    worker_private_ips = module.ec2_instances.worker_private_ips
    ssh_user           = "ubuntu"
    ssh_key_path       = var.ssh_private_key_path
  })
  filename = "${path.module}/../../scripts/generated/inventory.ini"
}

# Generate environment variables file
resource "local_file" "env_vars" {
  content = templatefile("${path.module}/templates/env-vars.sh.tpl", {
    aws_region         = var.aws_region
    aws_account_id     = data.aws_caller_identity.current.account_id
    ecr_registry       = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com"
    master_public_ip   = module.ec2_instances.master_public_ips[0]
    master_private_ip  = module.ec2_instances.master_private_ips[0]
    project_name       = var.project_name
    ecr_repositories   = module.ecr.repository_urls
  })
  filename = "${path.module}/../../scripts/generated/env-vars.sh"
}
