terraform {
  backend "s3" {
    bucket       = "petclinic-bt-staging-tfstate"
    key          = "petclinic-bt-staging/terraform.tfstate"
    region       = "us-east-1"
    use_lockfile = true
    encrypt      = true
  }
}
