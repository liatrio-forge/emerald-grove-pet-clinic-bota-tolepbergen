output "deploy_role_arn" {
  description = "ARN of the GitHub Actions deploy role — set as GitHub secret AWS_ROLE_ARN_DEPLOY_BT"
  value       = aws_iam_role.github_deploy.arn
}

output "deploy_role_name" {
  description = "Name of the GitHub Actions deploy IAM role"
  value       = aws_iam_role.github_deploy.name
}

output "oidc_provider_arn" {
  description = "ARN of the GitHub Actions OIDC provider"
  value       = aws_iam_openid_connect_provider.github_actions.arn
}

output "state_bucket_arn" {
  description = "ARN of the S3 bucket used for Terraform state"
  value       = aws_s3_bucket.terraform_state.arn
}

output "state_bucket_name" {
  description = "Name of the S3 bucket used for Terraform state"
  value       = aws_s3_bucket.terraform_state.bucket
}

output "ecr_repository_url" {
  description = "Full ECR repository URI — use as the base image URL in deploy workflows"
  value       = aws_ecr_repository.main.repository_url
}

output "ecr_repository_name" {
  description = "ECR repository name — set as GitHub variable ECR_REPOSITORY_BT"
  value       = aws_ecr_repository.main.name
}

output "ecr_repository_arn" {
  description = "ARN of the ECR repository — referenced by the IAM module in the main stack"
  value       = aws_ecr_repository.main.arn
}
