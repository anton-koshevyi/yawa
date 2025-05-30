## <ins>Y</ins>alantis <ins>AW</ins>S <ins>A</ins>cquaintance

Project involves building a microservices-based application hosted entirely on AWS. It includes two
main services: IAM and File. The IAM service handles basic user operations, while the File service
manages file uploads. The system uses AWS-managed services like RDS, DocumentDB, S3, SNS, SQS, SES,
Lambda, and Step Functions to implement asynchronous workflows, notifications, file management, etc.
Infrastructure is provisioned using Terraform.

### Functional Requirements

- User must be able to register and authenticate.
- User must be able to upload files, view the list of uploaded files, and download them.
- Files must be stored for 10 minutes before being deleted.
- User must be able to postpone file deletion.
- System must notify users upon upload completion and file deletion.

### Non-functional Requirements

- System must be implemented using a microservice architecture.
- Authentication must be implemented with Spring Security and JWT.
- User data must be stored in PostgreSQL hosted on Amazon RDS.
- Files must be stored in Amazon S3 and accessed via links.
- File metadata must be stored in MongoDB hosted on Amazon DocumentDB.
- Event-driven communication must be implemented using Amazon SNS.
- Remote Procedure Calls must be implemented using Amazon SQS.
- Emails must be sent via AWS Lambda using Amazon SES.
- AWS Step Functions must be used to schedule file deletion.
- AWS Step Functions must be managed by another service than file.
- Secrets must be managed using AWS Secrets Manager.
- Microservices must be deployed on separate Amazon EC2 instances.
- Deployment may be configured using any suitable tool.
- All services must be hosted in AWS.
- Infrastructure must be managed with Terraform.

---

## Summary of AWS Usage

| Service                                                          | Purpose / Usage                               |
|:-----------------------------------------------------------------|:----------------------------------------------|
| [EC2 - Elastic Compute Cloud](https://aws.amazon.com/ec2/)       | Hosting and running microservices             |
| [RDS - Relational Database Service](https://aws.amazon.com/rds/) | Storage of user data (PostgreSQL)             |
| [DocumentDB](https://aws.amazon.com/documentdb/)                 | Storage of file metadata (MongoDB)            |
| [S3 - Simple Storage Service](https://aws.amazon.com/s3/)        | Storage of user-uploaded files                |
| [SNS - Simple Notification Service](https://aws.amazon.com/sns/) | Event-driven communication                    |
| [SQS - Simple Queue Service](https://aws.amazon.com/sqs/)        | Remote Procedure Calls (RPC)                  |
| [SES - Simple Email Service](https://aws.amazon.com/ses/)        | Email service provider                        |
| [Lambda](https://aws.amazon.com/lambda/)                         | Sending emails via SES                        |
| [Step Functions](https://aws.amazon.com/step-functions/)         | Scheduling and orchestration of file deletion |
| [Secrets Manager](https://aws.amazon.com/secrets-manager/)       | Secure management of secrets and credentials  |
