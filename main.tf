# Files are structured according to the Terraform Style Guide, but merged into one for simplicity.
# Section comments separate different files. For more complex infrastructure, it is recommended to
# create a git repository for devops and split this file into different by corresponding comments.
# https://developer.hashicorp.com/terraform/language/style#file-names

# ===== terraform.tf =====

terraform {
  required_version = "~> 1.12.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }

    null = {
      source  = "hashicorp/null"
      version = "~> 3.0"
    }
  }
}

# ===== providers.tf =====

provider "aws" {}

# ===== variables.tf =====

variable "email_sender_address" {
  type     = string
  nullable = false
}

variable "environment_name" {
  type     = string
  nullable = false

  validation {
    condition     = contains(local.allowed_environment_names, var.environment_name)
    error_message = "Environment name must be one of predefined."
  }
}

variable "jar_path_email" {
  type     = string
  nullable = false
}

variable "jar_path_file" {
  type     = string
  nullable = false
}

variable "jar_path_iam" {
  type     = string
  nullable = false
}

variable "key_name" {
  type     = string
  nullable = false
}

variable "key_private_path" {
  type     = string
  nullable = false
}

variable "key_public_path" {
  type     = string
  nullable = false
}

variable "mongo_database_file" {
  type     = string
  default  = "yawa_file"
  nullable = false
}

variable "mongo_password_file" {
  type      = string
  ephemeral = true
  sensitive = true
}

variable "mongo_username_file" {
  type      = string
  sensitive = true
}

variable "postgres_database_iam" {
  type     = string
  default  = "yawa_iam"
  nullable = false
}

variable "postgres_password_iam" {
  type      = string
  ephemeral = true
  sensitive = true
}

variable "postgres_username_iam" {
  type      = string
  sensitive = true
}

# ===== locals.tf =====

locals {
  resource_prefix = "yawa-${var.environment_name}"
  jar_iam         = basename(var.jar_path_iam)
  jar_file        = basename(var.jar_path_file)

  route = {
    iam  = "iam-service"
    file = "file-service"
  }

  allowed_environment_names = ["development", "testing", "staging", "production"]
}

# ===== main.tf =====

resource "aws_iam_role" "default" {
  name = "YAWADefault"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          Service = [
            "ec2.amazonaws.com",
            "lambda.amazonaws.com",
            "states.amazonaws.com"
          ]
        },
        Action = "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "default" {
  for_each = toset([
    "arn:aws:iam::aws:policy/IAMFullAccess",
    "arn:aws:iam::aws:policy/SecretsManagerReadWrite",
    "arn:aws:iam::aws:policy/AmazonS3FullAccess",
    "arn:aws:iam::aws:policy/AmazonSNSFullAccess",
    "arn:aws:iam::aws:policy/AmazonSQSFullAccess",
    "arn:aws:iam::aws:policy/AmazonSESFullAccess",
    "arn:aws:iam::aws:policy/AWSStepFunctionsFullAccess",
    "arn:aws:iam::aws:policy/service-role/AWSLambdaRole",
    "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole",
  ])

  role       = aws_iam_role.default.name
  policy_arn = each.value
}

resource "aws_security_group" "app" {
  name = "${local.resource_prefix}-app"

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "data" {
  name = "${local.resource_prefix}-data"

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.app.id]
  }

  ingress {
    from_port       = 27017
    to_port         = 27017
    protocol        = "tcp"
    security_groups = [aws_security_group.app.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_db_instance" "iam" {
  identifier             = "${local.resource_prefix}-iam"
  instance_class         = "db.t3.micro"
  engine                 = "postgres"
  engine_version         = "13"
  db_name                = var.postgres_database_iam
  username               = var.postgres_username_iam
  password_wo            = var.postgres_password_iam
  password_wo_version    = 20250619211632
  allocated_storage      = 10
  skip_final_snapshot    = true
  vpc_security_group_ids = [aws_security_group.data.id]
}

resource "aws_docdb_cluster" "file" {
  cluster_identifier         = "${local.resource_prefix}-file-cluster"
  master_username            = var.mongo_username_file
  master_password_wo         = var.mongo_password_file
  master_password_wo_version = 20250619211632
  vpc_security_group_ids     = [aws_security_group.data.id]
  skip_final_snapshot        = true
}

resource "aws_docdb_cluster_instance" "file" {
  count = 1

  cluster_identifier = aws_docdb_cluster.file.id
  instance_class     = "db.t3.medium"

  tags = {
    Name = "${local.resource_prefix}-file-${count.index}"
  }
}

resource "aws_s3_bucket" "file" {
  bucket        = "${local.resource_prefix}-file"
  force_destroy = true
}

resource "aws_sns_topic" "email" {
  name = "${local.resource_prefix}-email"
}

resource "aws_sns_topic" "file_upload" {
  name = "${local.resource_prefix}-file-upload"
}

resource "aws_sns_topic" "file_delete" {
  name = "${local.resource_prefix}-file-delete"
}

resource "aws_sqs_queue" "user_get_by_id" {
  name       = "${local.resource_prefix}-user-get-by-id.fifo"
  fifo_queue = true
}

ephemeral "aws_secretsmanager_random_password" "session_access" {
  password_length = 32
}

ephemeral "aws_secretsmanager_random_password" "session_refresh" {
  password_length = 32
}

resource "aws_secretsmanager_secret" "session_access" {
  # Secret name will be unavailable for 7 days if deleted by Terraform.
  # For instant deletion without recovery use the aws console command:
  # aws secretsmanager delete-secret --secret-id <secret_name> --force-delete-without-recovery

  name = "${local.resource_prefix}-session-access"
}

resource "aws_secretsmanager_secret" "session_refresh" {
  # Secret name will be unavailable for 7 days if deleted by Terraform.
  # For instant deletion without recovery use the aws console command:
  # aws secretsmanager delete-secret --secret-id <secret_name> --force-delete-without-recovery

  name = "${local.resource_prefix}-session-refresh"
}

resource "aws_secretsmanager_secret_version" "session_access" {
  secret_id                = aws_secretsmanager_secret.session_access.id
  secret_string_wo         = ephemeral.aws_secretsmanager_random_password.session_access.random_password
  secret_string_wo_version = 20250618183724
}

resource "aws_secretsmanager_secret_version" "session_refresh" {
  secret_id                = aws_secretsmanager_secret.session_refresh.id
  secret_string_wo         = ephemeral.aws_secretsmanager_random_password.session_refresh.random_password
  secret_string_wo_version = 20250618183724
}

resource "aws_lambda_function" "email" {
  function_name    = "${local.resource_prefix}-email-sender"
  handler          = "com.example.yawa.email.EmailHandler"
  role             = aws_iam_role.default.arn
  runtime          = "java8.al2"
  memory_size      = 512
  timeout          = 30
  filename         = var.jar_path_email
  source_code_hash = filebase64sha256(var.jar_path_email)

  environment {
    variables = {
      APPLICATION_EMAIL_SENDER = var.email_sender_address
    }
  }
}

resource "aws_lambda_permission" "sns" {
  statement_id  = "AllowExecutionFromSNS"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.email.function_name
  principal     = "sns.amazonaws.com"
  source_arn    = aws_sns_topic.email.arn
}

resource "aws_key_pair" "default" {
  key_name   = var.key_name
  public_key = file(var.key_public_path)
}

resource "aws_iam_instance_profile" "default" {
  name = "default"
  role = aws_iam_role.default.name
}

resource "aws_instance" "iam" {
  ami                         = "ami-02003f9f0fde924ea"
  instance_type               = "t3.micro"
  key_name                    = aws_key_pair.default.key_name
  iam_instance_profile        = aws_iam_instance_profile.default.name
  vpc_security_group_ids      = [aws_security_group.app.id]
  associate_public_ip_address = true

  user_data = <<-EOF
              #!/bin/bash
              sudo apt-get update -y
              sudo apt-get install -y openjdk-8-jre
              EOF

  tags = {
    Name = "${local.resource_prefix}-iam"
  }
}

resource "aws_instance" "file" {
  ami                         = "ami-02003f9f0fde924ea"
  instance_type               = "t3.micro"
  key_name                    = aws_key_pair.default.key_name
  iam_instance_profile        = aws_iam_instance_profile.default.name
  vpc_security_group_ids      = [aws_security_group.app.id]
  associate_public_ip_address = true

  user_data = <<-EOF
              #!/bin/bash
              sudo apt-get update -y
              sudo apt-get install -y openjdk-8-jre
              EOF

  tags = {
    Name = "${local.resource_prefix}-file"
  }
}

resource "aws_apigatewayv2_api" "default" {
  name          = "yawa"
  protocol_type = "HTTP"
}

resource "aws_apigatewayv2_integration" "iam" {
  api_id             = aws_apigatewayv2_api.default.id
  integration_type   = "HTTP_PROXY"
  integration_method = "ANY"
  integration_uri    = "http://${aws_instance.iam.public_ip}:8080/{proxy}"
}

resource "aws_apigatewayv2_integration" "file" {
  api_id             = aws_apigatewayv2_api.default.id
  integration_type   = "HTTP_PROXY"
  integration_method = "ANY"
  integration_uri    = "http://${aws_instance.file.public_ip}:8080/{proxy}"
}

resource "aws_apigatewayv2_route" "iam" {
  api_id    = aws_apigatewayv2_api.default.id
  route_key = "ANY /${local.route.iam}/{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.iam.id}"
}

resource "aws_apigatewayv2_route" "file" {
  api_id    = aws_apigatewayv2_api.default.id
  route_key = "ANY /${local.route.file}/{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.file.id}"
}

resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.default.id
  name        = var.environment_name
  auto_deploy = true
}

resource "null_resource" "run_iam" {
  triggers = {
    always = timestamp()
  }

  connection {
    type        = "ssh"
    user        = "ubuntu"
    host        = aws_instance.iam.public_ip
    private_key = file(var.key_private_path)
  }

  provisioner "file" {
    source      = var.jar_path_iam
    destination = "/home/ubuntu/${local.jar_iam}"
  }

  provisioner "remote-exec" {
    inline = [
      "export APPLICATION_API_BASE_URL=${aws_apigatewayv2_stage.default.invoke_url}/${local.route.iam}",
      "export APPLICATION_AWS_LAMBDA_EMAIL_ARN=${aws_lambda_function.email.arn}",
      "export APPLICATION_AWS_SECRETS_MANAGER_SECRET_SESSION_ACCESS_NAME=${aws_secretsmanager_secret.session_access.name}",
      "export APPLICATION_AWS_SECRETS_MANAGER_SECRET_SESSION_REFRESH_NAME=${aws_secretsmanager_secret.session_refresh.name}",
      "export APPLICATION_AWS_SFN_STATE_MACHINE_FILE_DELETE_SCHEDULER_ROLE_ARN=${aws_iam_role.default.arn}",
      "export APPLICATION_AWS_SNS_TOPIC_EMAIL_ARN=${aws_sns_topic.email.arn}",
      "export APPLICATION_AWS_SNS_TOPIC_FILE_DELETE_ARN=${aws_sns_topic.file_delete.arn}",
      "export APPLICATION_AWS_SNS_TOPIC_FILE_UPLOAD_ARN=${aws_sns_topic.file_upload.arn}",
      "export APPLICATION_AWS_SQS_QUEUE_USER_GET_BY_ID_URL=${aws_sqs_queue.user_get_by_id.url}",
      "export SPRING_DATASOURCE_URL=jdbc:postgresql://${aws_db_instance.iam.endpoint}/${aws_db_instance.iam.db_name}",
      "export SPRING_DATASOURCE_USERNAME=${aws_db_instance.iam.username}",
      "export SPRING_DATASOURCE_PASSWORD=${var.postgres_password_iam}",
      "export SPRING_PROFILES_ACTIVE=${var.environment_name}",
      "pkill --full ${local.jar_iam}",
      "nohup java -jar ${local.jar_iam} &",
      "sleep 5"
    ]
  }
}

resource "null_resource" "run_file" {
  triggers = {
    always = timestamp()
  }

  connection {
    type        = "ssh"
    user        = "ubuntu"
    host        = aws_instance.file.public_ip
    private_key = file(var.key_private_path)
  }

  provisioner "file" {
    source      = var.jar_path_file
    destination = "/home/ubuntu/${local.jar_file}"
  }

  provisioner "remote-exec" {
    inline = [
      "export APPLICATION_API_BASE_URL=${aws_apigatewayv2_stage.default.invoke_url}/${local.route.file}",
      "export APPLICATION_AWS_LAMBDA_EMAIL_ARN=${aws_lambda_function.email.arn}",
      "export APPLICATION_AWS_S3_BUCKET_FILE=${aws_s3_bucket.file.bucket}",
      "export APPLICATION_AWS_SECRETS_MANAGER_SECRET_SESSION_ACCESS_NAME=${aws_secretsmanager_secret.session_access.name}",
      "export APPLICATION_AWS_SNS_TOPIC_EMAIL_ARN=${aws_sns_topic.email.arn}",
      "export APPLICATION_AWS_SNS_TOPIC_FILE_DELETE_ARN=${aws_sns_topic.file_delete.arn}",
      "export APPLICATION_AWS_SNS_TOPIC_FILE_UPLOAD_ARN=${aws_sns_topic.file_upload.arn}",
      "export APPLICATION_AWS_SQS_QUEUE_USER_GET_BY_ID_URL=${aws_sqs_queue.user_get_by_id.url}",
      "export SPRING_DATA_MONGODB_HOST=${aws_docdb_cluster.file.endpoint}",
      "export SPRING_DATA_MONGODB_PORT=${aws_docdb_cluster.file.port}",
      "export SPRING_DATA_MONGODB_DATABASE=${var.mongo_database_file}",
      "export SPRING_DATA_MONGODB_USERNAME=${aws_docdb_cluster.file.master_username}",
      "export SPRING_DATA_MONGODB_PASSWORD=${var.mongo_password_file}",
      "export SPRING_PROFILES_ACTIVE=${var.environment_name}",
      "pkill --full ${local.jar_file}",
      "nohup java -jar ${local.jar_file} &",
      "sleep 5"
    ]
  }
}
