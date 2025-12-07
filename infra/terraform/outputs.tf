# Terraform Outputs for Enterprise Kubernetes Stack

output "vpc_id" {
  description = "ID of the created VPC"
  value       = module.vpc.vpc_id
}

output "master_public_ips" {
  description = "Public IP addresses of K8s master nodes"
  value       = module.ec2_instances.master_public_ips
}

output "master_private_ips" {
  description = "Private IP addresses of K8s master nodes"
  value       = module.ec2_instances.master_private_ips
}

output "worker_public_ips" {
  description = "Public IP addresses of K8s worker nodes"
  value       = module.ec2_instances.worker_public_ips
}

output "worker_private_ips" {
  description = "Private IP addresses of K8s worker nodes"
  value       = module.ec2_instances.worker_private_ips
}

output "ecr_repository_urls" {
  description = "URLs of ECR repositories"
  value       = module.ecr.repository_urls
}

output "aws_region" {
  description = "AWS region"
  value       = var.aws_region
}

output "aws_account_id" {
  description = "AWS Account ID"
  value       = data.aws_caller_identity.current.account_id
}

output "ecr_registry" {
  description = "ECR registry URL"
  value       = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com"
}

output "frontend_url" {
  description = "URL to access the frontend (after deployment)"
  value       = "http://${module.ec2_instances.master_public_ips[0]}:30080"
}

output "argocd_url" {
  description = "URL to access Argo CD UI"
  value       = "https://${module.ec2_instances.master_public_ips[0]}:30443"
}

output "ssh_commands" {
  description = "SSH commands to connect to nodes"
  value = {
    master = "ssh -i ${var.ssh_private_key_path} ubuntu@${module.ec2_instances.master_public_ips[0]}"
    workers = [for ip in module.ec2_instances.worker_public_ips : "ssh -i ${var.ssh_private_key_path} ubuntu@${ip}"]
  }
}
