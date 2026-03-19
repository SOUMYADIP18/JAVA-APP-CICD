terraform {
  # Add this new backend block!
  backend "s3" {
    bucket = "terraform-state-soumyadip-12345" # Use the exact name you just created
    key    = "infrastructure/terraform.tfstate"
    region = "ap-south-1"
  }
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# Tell Terraform which region to build in
provider "aws" {
  region = "ap-south-1"
}

# 1. Create the Security Group (Firewall Rules)
resource "aws_security_group" "java_app_sg" {
  name        = "java-app-security-group"
  description = "Allow SSH and Port 8080"

  # Allow SSH (Port 22)
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Allow Java App (Port 8080)
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Allow server to connect to the internet (Download updates/Docker)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# 2. Automatically find the latest Ubuntu 22.04 AMI
data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical (Ubuntu)

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }
}

# 3. Create the EC2 Instance
resource "aws_instance" "java_app_server" {
  ami           = data.aws_ami.ubuntu.id
  instance_type = "t3.micro"
  key_name      = "devops_server_keypair"

  # Attach the security group from Step 1
  vpc_security_group_ids = [aws_security_group.java_app_sg.id]

  # This script runs exactly once when the server boots to install Docker
  user_data = <<-EOF
              #!/bin/bash
              sudo apt-get update -y
              sudo apt-get install docker.io -y
              sudo systemctl start docker
              sudo systemctl enable docker
              sudo usermod -aG docker ubuntu
              EOF

  tags = {
    Name = "Terraform-Java-Server"
  }
}
# Output the Public IP address of the server
output "server_public_ip" {
  value = aws_instance.java_app_server.public_ip
}