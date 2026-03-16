variable "aws_region" {
  type        = string
  description = "AWS region for all resources"
  default     = "us-east-1"
}

variable "environment" {
  type        = string
  description = "Deployment environment name"
  default     = "staging"
}

variable "github_repository" {
  type        = string
  description = "GitHub repository in 'owner/repo' format for OIDC subject claim"
  default     = "liatrio-forge/emerald-grove-pet-clinic-bota-tolepbergen"
}

variable "project_name" {
  type        = string
  description = "Project name used as prefix for all resources"
  default     = "petclinic-bt"
}
