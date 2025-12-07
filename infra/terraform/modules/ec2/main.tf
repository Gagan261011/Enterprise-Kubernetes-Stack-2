# EC2 Module for Enterprise Kubernetes Stack

variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "instance_type" {
  type = string
}

variable "key_name" {
  type = string
}

variable "master_count" {
  type = number
}

variable "worker_count" {
  type = number
}

variable "subnet_ids" {
  type = list(string)
}

variable "master_security_group" {
  type = string
}

variable "worker_security_group" {
  type = string
}

variable "common_tags" {
  type = map(string)
}

# Get latest Ubuntu 22.04 AMI
data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# IAM Role for EC2 instances (for ECR access)
resource "aws_iam_role" "k8s_node" {
  name = "${var.project_name}-k8s-node-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })

  tags = var.common_tags
}

# IAM Policy for ECR access
resource "aws_iam_role_policy" "ecr_access" {
  name = "${var.project_name}-ecr-access"
  role = aws_iam_role.k8s_node.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken",
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:GetRepositoryPolicy",
          "ecr:DescribeRepositories",
          "ecr:ListImages",
          "ecr:DescribeImages",
          "ecr:BatchGetImage",
          "ecr:GetLifecyclePolicy",
          "ecr:GetLifecyclePolicyPreview",
          "ecr:ListTagsForResource",
          "ecr:DescribeImageScanFindings"
        ]
        Resource = "*"
      }
    ]
  })
}

# IAM Instance Profile
resource "aws_iam_instance_profile" "k8s_node" {
  name = "${var.project_name}-k8s-node-profile"
  role = aws_iam_role.k8s_node.name
}

# Master Node(s)
resource "aws_instance" "master" {
  count                  = var.master_count
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = var.instance_type
  key_name               = var.key_name
  subnet_id              = var.subnet_ids[count.index % length(var.subnet_ids)]
  vpc_security_group_ids = [var.master_security_group]
  iam_instance_profile   = aws_iam_instance_profile.k8s_node.name

  root_block_device {
    volume_size = 50
    volume_type = "gp3"
    encrypted   = true
  }

  user_data = base64encode(templatefile("${path.module}/templates/node-init.sh", {
    node_type = "master"
    node_name = "k8s-master-${count.index + 1}"
  }))

  tags = merge(var.common_tags, {
    Name     = "${var.project_name}-master-${count.index + 1}"
    NodeType = "master"
  })
}

# Worker Nodes
resource "aws_instance" "worker" {
  count                  = var.worker_count
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = var.instance_type
  key_name               = var.key_name
  subnet_id              = var.subnet_ids[count.index % length(var.subnet_ids)]
  vpc_security_group_ids = [var.worker_security_group]
  iam_instance_profile   = aws_iam_instance_profile.k8s_node.name

  root_block_device {
    volume_size = 50
    volume_type = "gp3"
    encrypted   = true
  }

  user_data = base64encode(templatefile("${path.module}/templates/node-init.sh", {
    node_type = "worker"
    node_name = "k8s-worker-${count.index + 1}"
  }))

  tags = merge(var.common_tags, {
    Name     = "${var.project_name}-worker-${count.index + 1}"
    NodeType = "worker"
  })
}

# Outputs
output "master_public_ips" {
  value = aws_instance.master[*].public_ip
}

output "master_private_ips" {
  value = aws_instance.master[*].private_ip
}

output "worker_public_ips" {
  value = aws_instance.worker[*].public_ip
}

output "worker_private_ips" {
  value = aws_instance.worker[*].private_ip
}

output "master_instance_ids" {
  value = aws_instance.master[*].id
}

output "worker_instance_ids" {
  value = aws_instance.worker[*].id
}
