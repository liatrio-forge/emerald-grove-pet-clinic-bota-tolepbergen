variable "db_name" {
  type        = string
  description = "Database name"
  default     = "petclinic"
}

variable "db_username" {
  type        = string
  description = "Database master username"
  default     = "petclinic"
}

variable "instance_class" {
  type        = string
  description = "RDS instance class"
  default     = "db.t3.micro"
}

variable "name_prefix" {
  type        = string
  description = "Prefix for resource names"
}

variable "security_group_id" {
  type        = string
  description = "Security group ID for the RDS instance"
}

variable "subnet_ids" {
  type        = list(string)
  description = "Subnet IDs for the DB subnet group"
}

variable "tags" {
  type        = map(string)
  description = "Common tags"
  default     = {}
}
