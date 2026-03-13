variable "availability_zones" {
  type        = list(string)
  description = "List of availability zones to use. Must be valid AZs in the chosen aws_region."
  default     = []
}

variable "aws_region" {
  type        = string
  description = "AWS region for all resources"
  default     = "us-east-1"
}

variable "container_image" {
  type        = string
  description = "Docker image URI for the application container"

  validation {
    condition     = length(var.container_image) > 0
    error_message = "container_image must be a non-empty Docker image URI."
  }
}

variable "container_port" {
  type        = number
  description = "Port the application container listens on"
  default     = 8080

  validation {
    condition     = var.container_port >= 1 && var.container_port <= 65535
    error_message = "container_port must be between 1 and 65535."
  }
}

variable "db_instance_class" {
  type        = string
  description = "RDS instance class"
  default     = "db.t3.micro"
}

variable "desired_count" {
  type        = number
  description = "Desired number of ECS tasks"
  default     = 1

  validation {
    condition     = var.desired_count >= 0
    error_message = "desired_count must be >= 0."
  }
}

variable "ecs_cpu" {
  type        = number
  description = "Fargate CPU units for the ECS task"
  default     = 512

  validation {
    condition     = contains([256, 512, 1024, 2048, 4096], var.ecs_cpu)
    error_message = "ecs_cpu must be a supported Fargate value: 256, 512, 1024, 2048, or 4096."
  }
}

variable "ecs_memory" {
  type        = number
  description = "Fargate memory (MiB) for the ECS task"
  default     = 1024

  validation {
    condition     = contains([512, 1024, 2048, 3072, 4096, 5120, 6144, 7168, 8192, 16384, 30720], var.ecs_memory)
    error_message = "ecs_memory must be a supported Fargate value (512–30720 MiB)."
  }
}

variable "environment" {
  type        = string
  description = "Deployment environment name"
  default     = "staging"

  validation {
    condition     = contains(["staging", "production"], var.environment)
    error_message = "Environment must be 'staging' or 'production'."
  }
}

variable "project_name" {
  type        = string
  description = "Project name used as prefix for all resources"
  default     = "petclinic-bt"
}

variable "vpc_cidr" {
  type        = string
  description = "CIDR block for the VPC"
  default     = "10.0.0.0/16"

  validation {
    condition     = can(cidrhost(var.vpc_cidr, 0))
    error_message = "vpc_cidr must be a valid CIDR block (e.g. 10.0.0.0/16)."
  }
}
