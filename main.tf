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
# 1. Get your default Virtual Private Cloud (VPC)
data "aws_vpc" "default" {
  default = true
}

# 2. Get the subnets inside that VPC (so the ALB can span multiple zones)
data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}
# 3. Security Group specifically for the Load Balancer
resource "aws_security_group" "alb_sg" {
  name        = "alb-security-group"
  description = "Allow standard HTTP web traffic from the internet"
  vpc_id      = data.aws_vpc.default.id

  # Inbound rule: Let anyone in the world hit Port 80
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Outbound rule: Let the ALB talk to your EC2 instance
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}
# 4. The Application Load Balancer
resource "aws_lb" "java_app_alb" {
  name               = "java-app-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb_sg.id] # Attaches the firewall from Step 2
  subnets            = data.aws_subnets.default.ids   # Spreads it across multiple zones
}

# 5. The Target Group (Routes traffic to port 8080)
resource "aws_lb_target_group" "java_app_tg" {
  name     = "java-app-tg"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = data.aws_vpc.default.id

  # The ALB will ping your app every 30 seconds to make sure it hasn't crashed!
  health_check {
    path                = "/"
    healthy_threshold   = 2
    unhealthy_threshold = 2
    timeout             = 3
    interval            = 30
  }
}
# 6. The Listener (Listens on Port 80, forwards to the Target Group)
resource "aws_lb_listener" "front_end" {
  load_balancer_arn = aws_lb.java_app_alb.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.java_app_tg.arn
  }
}

# 7. The Glue: Attach your EC2 instance to the Target Group
resource "aws_lb_target_group_attachment" "java_app_attach" {
  target_group_arn = aws_lb_target_group.java_app_tg.arn
  target_id        = aws_instance.java_app_server.id # Matches your EC2 instance name!
  port             = 8080
}

# 1. Create the Security Group (Firewall Rules)
resource "aws_security_group" "java_app_sg" {
  name        = "java-app-security-group-v2"
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
output "alb_dns_name" {
  description = "The permanent URL of your Load Balancer"
  value       = aws_lb.java_app_alb.dns_name
}
# 8. AWS SSM Parameter Store (The Secret Vault)
resource "aws_ssm_parameter" "ec2_public_ip" {
  name        = "/java-app/production/ec2-public-ip"
  description = "The public IP of the Java App EC2 instance"
  type        = "String"
  value       = aws_instance.java_app_server.public_ip
}