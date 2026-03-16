variable "aws_region" {
  type        = string
  description = "AWS region for CloudWatch logs"
}

variable "container_image" {
  type        = string
  description = "Docker image URI"
}

variable "container_port" {
  type        = number
  description = "Container port"
  default     = 8080
}

variable "cpu" {
  type        = number
  description = "Fargate CPU units"
  default     = 512
}

variable "db_name" {
  type        = string
  description = "Database name"
}

variable "db_secret_arn" {
  type        = string
  description = "Secrets Manager ARN for DB password"
}

variable "db_username" {
  type        = string
  description = "Database username"
}

variable "desired_count" {
  type        = number
  description = "Desired number of tasks"
  default     = 1
}

variable "execution_role_arn" {
  type        = string
  description = "ECS task execution role ARN"
}

variable "health_check_grace_period" {
  type        = number
  description = "Health check grace period seconds"
  default     = 120
}

variable "memory" {
  type        = number
  description = "Fargate memory in MiB"
  default     = 1024
}

variable "name_prefix" {
  type        = string
  description = "Prefix for resource names"
}

variable "private_subnet_ids" {
  type        = list(string)
  description = "Private subnet IDs for ECS tasks"
}

variable "rds_endpoint" {
  type        = string
  description = "RDS endpoint (host:port)"
}

variable "security_group_id" {
  type        = string
  description = "Security group ID for ECS tasks"
}

variable "tags" {
  type        = map(string)
  description = "Common tags"
  default     = {}
}

variable "target_group_arn" {
  type        = string
  description = "ALB target group ARN"
}

variable "task_role_arn" {
  type        = string
  description = "ECS task role ARN"
}
