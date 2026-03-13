#!/usr/bin/env bash
#
# Bootstrap script: creates S3 state bucket, OIDC provider, IAM deploy role,
# and ECR repository via Terraform, then migrates local state into the S3 bucket.
#
# Usage: cd terraform/bootstrap && ./bootstrap.sh
#
# Teardown: remove backend.tf, run terraform init -migrate-state, then terraform destroy
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

echo "==> Step 1: Initialise Terraform with local state"
terraform init

echo ""
echo "==> Applying bootstrap resources (S3 bucket + OIDC provider + IAM role)"
terraform apply

echo ""
echo "==> Step 2: Migrate local state to S3"
echo "    Copy the example backend config and run init -migrate-state:"
echo ""
echo "    cp backend.tf.example backend.tf"
echo "    terraform init -migrate-state"
echo ""
read -r -p "Press Enter after copying backend.tf.example to backend.tf, or Ctrl-C to do it manually later..."

if [ ! -f backend.tf ]; then
  echo "ERROR: backend.tf not found. Copy backend.tf.example to backend.tf and re-run:"
  echo "  cp backend.tf.example backend.tf && terraform init -migrate-state"
  exit 1
fi

terraform init -migrate-state

echo ""
echo "==> Bootstrap complete. Outputs:"
terraform output

echo ""
echo "Next steps:"
echo "  1. Set GitHub secret AWS_ROLE_ARN_DEPLOY_BT to the deploy_role_arn output above"
echo "  2. Set GitHub secret TF_BACKEND_BUCKET_BT to the state_bucket_name output above"
echo "  3. Set GitHub secret TF_BACKEND_KEY_BT to the desired state file key (e.g. petclinic/terraform.tfstate)"
echo "  4. Set GitHub variable AWS_REGION_BT to your region (default: us-east-1)"
echo "  5. Set GitHub variable AWS_ACCOUNT_ID_BT to your AWS account ID"
echo "  6. Set GitHub variable ECR_REPOSITORY_BT to the ecr_repository_name output above"
