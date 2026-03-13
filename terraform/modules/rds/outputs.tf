output "db_endpoint" {
  description = "The connection endpoint for the RDS instance"
  value       = aws_db_instance.main.endpoint
}

output "db_name" {
  description = "The database name"
  value       = aws_db_instance.main.db_name
}

output "db_username" {
  description = "The master username for the database"
  value       = aws_db_instance.main.username
}

output "db_secret_arn" {
  description = "ARN of the Secrets Manager secret containing the DB password"
  value       = aws_secretsmanager_secret.db_password.arn
}
