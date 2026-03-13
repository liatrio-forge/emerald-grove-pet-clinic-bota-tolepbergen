locals {
  name_prefix = "${var.project_name}-${var.environment}"

  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "terraform"
  }

  oidc_issuer_url = "https://token.actions.githubusercontent.com"
  github_subject  = "repo:${var.github_repository}:ref:refs/heads/main"
}
