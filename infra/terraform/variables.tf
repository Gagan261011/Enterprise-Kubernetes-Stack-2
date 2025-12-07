# Terraform Variables for Enterprise Kubernetes Stack

variable "aws_region" {
  description = "AWS region for all resources"
  type        = string
  default     = "ap-south-1"
}

variable "project_name" {
  description = "Name of the project (used for resource naming)"
  type        = string
  default     = "enterprise-k8s-shop"
}

variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string
  default     = "demo"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "pod_network_cidr" {
  description = "CIDR block for Kubernetes pod network"
  type        = string
  default     = "192.168.0.0/16"
}

variable "service_cidr" {
  description = "CIDR block for Kubernetes service network"
  type        = string
  default     = "10.96.0.0/12"
}

variable "instance_type" {
  description = "EC2 instance type for K8s nodes"
  type        = string
  default     = "t3.medium"
}

variable "master_count" {
  description = "Number of K8s master nodes"
  type        = number
  default     = 1
}

variable "worker_count" {
  description = "Number of K8s worker nodes"
  type        = number
  default     = 3
}

variable "key_name" {
  description = "Name of the AWS key pair for SSH access"
  type        = string
}

variable "ssh_private_key_path" {
  description = "Path to the SSH private key file"
  type        = string
  default     = "~/.ssh/id_rsa"
}

variable "allowed_ssh_ip" {
  description = "IP address allowed to SSH into instances (your IP)"
  type        = string
  default     = "0.0.0.0/0" # Change this to your IP for security
}
