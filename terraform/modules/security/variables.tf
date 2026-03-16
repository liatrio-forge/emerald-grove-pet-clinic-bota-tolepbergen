variable "container_port" {
  type        = number
  description = "Port the container listens on"
  default     = 8080
}

variable "name_prefix" {
  type        = string
  description = "Prefix for resource names"
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
