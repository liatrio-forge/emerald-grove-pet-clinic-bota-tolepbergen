terraform {
  backend "s3" {
    bucket       = "PLACEHOLDER"
    key          = "PLACEHOLDER"
    region       = "us-east-1"
    use_lockfile = true
    encrypt      = true
  }
}
