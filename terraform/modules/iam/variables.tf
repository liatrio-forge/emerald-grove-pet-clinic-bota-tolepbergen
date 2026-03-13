variable "db_secret_arn" {
  type        = string
  description = "ARN of the Secrets Manager secret for DB password"
}

variable "ecr_repository_arn" {
  type        = string
  description = "ARN of the ECR repository"
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
