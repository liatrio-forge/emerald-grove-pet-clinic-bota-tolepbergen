locals {
  name_prefix = "${var.project_name}-${var.environment}"

  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "terraform"
  }

  # Valid Fargate memory (MiB) per CPU value — per AWS ECS Fargate task size docs.
  valid_fargate_memory = {
    "256"  = [512, 1024, 2048]
    "512"  = [1024, 2048, 3072, 4096]
    "1024" = [2048, 3072, 4096, 5120, 6144, 7168, 8192]
    "2048" = [4096, 5120, 6144, 7168, 8192, 9216, 10240, 11264, 12288, 13312, 14336, 15360, 16384]
    "4096" = [8192, 9216, 10240, 11264, 12288, 13312, 14336, 15360, 16384, 17408, 18432, 19456, 20480, 21504, 22528, 23552, 24576, 25600, 26624, 27648, 28672, 29696, 30720]
  }
}

check "fargate_cpu_memory_compatibility" {
  assert {
    condition     = contains(local.valid_fargate_memory[tostring(var.ecs_cpu)], var.ecs_memory)
    error_message = "Invalid Fargate CPU/memory combination: cpu=${var.ecs_cpu}, memory=${var.ecs_memory} MiB. See https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task-cpu-memory-error.html for valid pairs."
  }
}
