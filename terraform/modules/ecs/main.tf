resource "aws_cloudwatch_log_group" "main" {
  name              = "/ecs/${var.name_prefix}"
  retention_in_days = 7

  tags = merge(var.tags, { Name = "${var.name_prefix}-logs" })
}

resource "aws_ecs_cluster" "main" {
  name = var.name_prefix

  tags = merge(var.tags, { Name = "${var.name_prefix}" })
}

resource "aws_ecs_task_definition" "main" {
  family                   = var.name_prefix
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.cpu
  memory                   = var.memory
  execution_role_arn       = var.execution_role_arn
  task_role_arn            = var.task_role_arn

  container_definitions = jsonencode([{
    name      = var.name_prefix
    image     = var.container_image
    essential = true

    portMappings = [
      {
        containerPort = var.container_port
        protocol      = "tcp"
      }
    ]

    environment = [
      { name = "SPRING_PROFILES_ACTIVE", value = "postgres" },
      { name = "POSTGRES_URL", value = "jdbc:postgresql://${var.rds_endpoint}/${var.db_name}" },
      { name = "POSTGRES_USER", value = var.db_username }
    ]

    secrets = [
      { name = "POSTGRES_PASS", valueFrom = var.db_secret_arn }
    ]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.main.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "ecs"
      }
    }

    healthCheck = {
      command     = ["CMD-SHELL", "curl -f http://localhost:${var.container_port}/actuator/health || exit 1"]
      interval    = 30
      timeout     = 5
      retries     = 3
      startPeriod = 60
    }
  }])

  tags = merge(var.tags, { Name = "${var.name_prefix}-task" })
}

resource "aws_ecs_service" "main" {
  name            = var.name_prefix
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.main.arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  health_check_grace_period_seconds = var.health_check_grace_period

  network_configuration {
    subnets          = var.private_subnet_ids
    security_groups  = [var.security_group_id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = var.target_group_arn
    container_name   = var.name_prefix
    container_port   = var.container_port
  }

  lifecycle {
    ignore_changes = [task_definition]
  }

  tags = merge(var.tags, { Name = "${var.name_prefix}-service" })
}
