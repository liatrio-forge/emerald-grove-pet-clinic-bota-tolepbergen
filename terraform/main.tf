data "aws_availability_zones" "available" {
  state = "available"
}

locals {
  # Use provided AZs, or fall back to the first two available in the region
  resolved_azs = length(var.availability_zones) > 0 ? var.availability_zones : slice(data.aws_availability_zones.available.names, 0, 2)
}

module "networking" {
  source = "./modules/networking"

  name_prefix        = local.name_prefix
  vpc_cidr           = var.vpc_cidr
  availability_zones = local.resolved_azs
  tags               = local.common_tags
}

module "security" {
  source = "./modules/security"

  name_prefix    = local.name_prefix
  vpc_id         = module.networking.vpc_id
  container_port = var.container_port
  tags           = local.common_tags
}

data "aws_ecr_repository" "main" {
  name = local.name_prefix
}

module "iam" {
  source = "./modules/iam"

  name_prefix        = local.name_prefix
  ecr_repository_arn = data.aws_ecr_repository.main.arn
  db_secret_arn      = module.rds.db_secret_arn
  tags               = local.common_tags
}

module "rds" {
  source = "./modules/rds"

  name_prefix       = local.name_prefix
  subnet_ids        = module.networking.private_subnet_ids
  security_group_id = module.security.rds_security_group_id
  instance_class    = var.db_instance_class
  tags              = local.common_tags
}

module "alb" {
  source = "./modules/alb"

  name_prefix       = local.name_prefix
  vpc_id            = module.networking.vpc_id
  public_subnet_ids = module.networking.public_subnet_ids
  security_group_id = module.security.alb_security_group_id
  container_port    = var.container_port
  tags              = local.common_tags
}

module "ecs" {
  source = "./modules/ecs"

  name_prefix        = local.name_prefix
  aws_region         = var.aws_region
  container_image    = var.container_image
  container_port     = var.container_port
  cpu                = var.ecs_cpu
  memory             = var.ecs_memory
  desired_count      = var.desired_count
  private_subnet_ids = module.networking.private_subnet_ids
  security_group_id  = module.security.ecs_security_group_id
  target_group_arn   = module.alb.target_group_arn
  execution_role_arn = module.iam.task_execution_role_arn
  task_role_arn      = module.iam.task_role_arn
  rds_endpoint       = module.rds.db_endpoint
  db_name            = module.rds.db_name
  db_username        = module.rds.db_username
  db_secret_arn      = module.rds.db_secret_arn
  tags               = local.common_tags
}
