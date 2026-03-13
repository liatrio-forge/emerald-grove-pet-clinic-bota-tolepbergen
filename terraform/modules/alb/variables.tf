variable "container_port" {
  type        = number
  description = "Port the container listens on"
  default     = 8080
}

variable "health_check_path" {
  type        = string
  description = "Health check path"
  default     = "/actuator/health"
}

variable "name_prefix" {
  type        = string
  description = "Prefix for resource names"
}

variable "public_subnet_ids" {
  type        = list(string)
  description = "Public subnet IDs for the ALB"
}

variable "security_group_id" {
  type        = string
  description = "Security group ID for the ALB"
}

variable "tags" {
  type        = map(string)
  description = "Common tags"
  default     = {}
}

variable "vpc_id" {
  type        = string
  description = "VPC ID"
}
