output "app_url" {
  description = "Public URL of the application (ALB DNS name)"
  value       = "http://${module.alb.alb_dns_name}"
}

output "ecr_repository_url" {
  description = "URL of the ECR repository for Docker images"
  value       = data.aws_ecr_repository.main.repository_url
}

output "ecs_cluster_name" {
  description = "Name of the ECS cluster"
  value       = module.ecs.cluster_name
}

output "ecs_service_name" {
  description = "Name of the ECS service"
  value       = module.ecs.service_name
}

output "rds_endpoint" {
  description = "RDS PostgreSQL connection endpoint"
  value       = module.rds.db_endpoint
}
