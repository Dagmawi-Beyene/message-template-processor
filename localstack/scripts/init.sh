#!/bin/bash
set -e  # Exit on any error

echo 'Starting LocalStack initialization...'

# Function to check if a command exists
check_command() {
    if ! command -v "$1" &> /dev/null; then
        echo "Error: $1 is required but not installed."
        exit 1
    fi
}

# Function to retry commands
retry() {
    local retries=3
    local count=1
    until "$@"; do
        count=$((count + 1))
        if [ $count -gt $retries ]; then
            echo "Failed after $retries attempts"
            return 1
        fi
        echo "Command failed, retrying ($count/$retries)..."
        sleep 2
    done
}

# Check required commands
check_command awslocal

# Create S3 bucket
echo "Creating S3 bucket..."
retry awslocal s3 mb s3://s3-bucket --region eu-west-1
echo "✓ S3 bucket created"

# Create DynamoDB table
echo "Creating DynamoDB table..."
retry awslocal dynamodb create-table \
    --cli-input-json file://../data/dynamodb-table-definition.json
echo "✓ DynamoDB table created"

# Create DLQ
echo "Creating Dead Letter Queue..."
retry awslocal sqs create-queue --queue-name generic-dlq
echo "✓ DLQ created"

# Create main queue with DLQ configuration
echo "Creating main SQS queue..."
retry awslocal sqs create-queue \
    --queue-name s3-notifications-queue \
    --attributes "{\"RedrivePolicy\": \"{\\\"deadLetterTargetArn\\\":\\\"arn:aws:sqs:eu-west-1:000000000000:generic-dlq\\\",\\\"maxReceiveCount\\\":\\\"1\\\"}\"}"
echo "✓ Main queue created"

# Configure S3 bucket notifications
echo "Configuring S3 bucket notifications..."
retry awslocal s3api put-bucket-notification \
    --bucket s3-bucket \
    --notification-configuration file://../data/s3-bucket-notifications.json
echo "✓ S3 notifications configured"

# Purge any test messages
echo "Purging test messages..."
retry awslocal sqs purge-queue \
    --queue-url http://sqs.eu-west-1.localhost:4566/000000000000/s3-notifications-queue
echo "✓ Queue purged"

# Verify setup
echo "Verifying setup..."

# Check S3
if awslocal s3 ls s3://s3-bucket &>/dev/null; then
    echo "✓ S3 bucket verification successful"
else
    echo "✗ S3 bucket verification failed"
    exit 1
fi

# Check DynamoDB
if awslocal dynamodb describe-table --table-name dynamodb-table &>/dev/null; then
    echo "✓ DynamoDB table verification successful"
else
    echo "✗ DynamoDB table verification failed"
    exit 1
fi

# Check SQS queues
if awslocal sqs get-queue-url --queue-name s3-notifications-queue &>/dev/null; then
    echo "✓ Main queue verification successful"
else
    echo "✗ Main queue verification failed"
    exit 1
fi

if awslocal sqs get-queue-url --queue-name generic-dlq &>/dev/null; then
    echo "✓ DLQ verification successful"
else
    echo "✗ DLQ verification failed"
    exit 1
fi

echo "✓ LocalStack initialization completed successfully"
