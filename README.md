# Message Template Processor

A Spring Boot application that processes message templates from a CMS system, transforms them, and stores them in DynamoDB. The application listens to S3 events via SQS and handles template updates automatically.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Technology Stack](#technology-stack)
- [Project Setup](#project-setup)
- [Local Development](#local-development)
- [Testing](#testing)
- [API Documentation](#api-documentation)
- [Architecture](#architecture)
- [Troubleshooting](#troubleshooting)

## Prerequisites

- Java 17 (LTS)
- Maven 3.8+
- Docker and Docker Compose
- Git

## Technology Stack

- Spring Boot 3.2.3
- AWS SDK v2
- LocalStack 3.2.0
- JUnit 5
- Mockito
- Lombok
- Jackson

## Project Setup

1. Clone the repository:
```bash
git clone https://github.com/Dagmawi-Beyene/message-template-processor.git
cd message-template-processor
```

2. Run the setup script:
```bash
chmod +x localstack/scripts/setup.sh
./localstack/scripts/setup.sh
```

This script will:
- Build the project
- Start LocalStack
- Verify AWS services are running

## Local Development

### Configuration

The application uses:
- `application.properties`: Main configuration file

### Running the Application

1. Start the application:
```bash
./mvnw spring-boot:run
```

2. Test template processing:
```bash
# Upload a template to S3
aws --endpoint-url=http://localhost:4566 s3 cp \
    template.json s3://s3-bucket/templates/

# Check DynamoDB for the processed template
aws --endpoint-url=http://localhost:4566 dynamodb scan \
    --table-name dynamodb-table
```

### LocalStack Services

The following AWS services are emulated locally:
- S3 Bucket: `s3-bucket`
- SQS Queues: 
  - Main: `s3-notifications-queue`
  - DLQ: `generic-dlq`
- DynamoDB Table: `dynamodb-table`

## Testing

### Running Tests

Run the test script:
```bash
chmod +x localstack/scripts/run-tests.sh
./localstack/scripts/run-tests.sh
```

This will:
- Run unit tests
- Generate coverage reports in `target/site/jacoco/index.html`

### Template Update Testing

The project includes a test script to demonstrate how templates are updated when a new version is uploaded with the same ID.

1. Run the update test script:
```bash
chmod +x localstack/scripts/test-template-updates.sh
./localstack/scripts/test-template-updates.sh
```

The script demonstrates:
- Template creation and updates using the same ID
- Processing of complex CMS template structures
- Proper handling of linked entries (traffic types, parameters)
- Full pipeline functionality (S3 → SQS → Application → DynamoDB)

Example test flow:
1. Uploads initial template (V1):
```json
{
  "items": [{
    "sys": { "id": "111-111-111" },
    "fields": {
      "key": "TRAIN_DELAYED",
      "name": "Train Delayed V1",
      "body": "Train {TRAIN_TYPE} is delayed by 10 minutes."
    }
  }],
  "includes": {
    "Entry": [{
      "sys": { "id": "333-333-333" },
      "fields": {
        "key": "TRAIN_TYPE",
        "name": "Train Type"
      }
    }]
  }
}
```

2. Updates with V2 (same ID):
```json
{
  "items": [{
    "sys": { "id": "111-111-111" },
    "fields": {
      "key": "TRAIN_DELAYED",
      "name": "Train Delayed V2",
      "body": "Updated: Train {TRAIN_TYPE} is now delayed by 20 minutes."
    }
  }],
  "includes": {
    "Entry": [{
      "sys": { "id": "333-333-333" },
      "fields": {
        "key": "TRAIN_TYPE",
        "name": "Train Type"
      }
    }]
  }
}
```

### Manual Testing

1. Upload a template:
```bash
aws --endpoint-url=http://localhost:4566 s3 cp \
    src/test/resources/samples/template.json \
    s3://s3-bucket/templates/
```

2. Check SQS message:
```bash
aws --endpoint-url=http://localhost:4566 sqs receive-message \
    --queue-url http://localhost:4566/000000000000/s3-notifications-queue
```

3. Verify DynamoDB:
```bash
aws --endpoint-url=http://localhost:4566 dynamodb scan \
    --table-name dynamodb-table
```

## API Documentation

### DynamoDB Schema

Primary Table Structure:
```
PK (Hash)                    | SK (Range)                  | Attributes
----------------------------|----------------------------|------------
TEMPLATE#{templateId}        | METADATA#{trafficType}     | template data
TRAFFICTYPE#{trafficType}   | TEMPLATE#{templateId}      | template data
```

### Access Patterns

1. Find template by ID:
```java
querySpec = new QuerySpec()
    .withKeyConditionExpression("PK = :pk")
    .withValueMap(new ValueMap()
        .withString(":pk", "TEMPLATE#" + templateId));
```

2. Find templates by traffic type:
```java
querySpec = new QuerySpec()
    .withKeyConditionExpression("PK = :pk")
    .withValueMap(new ValueMap()
        .withString(":pk", "TRAFFICTYPE#" + trafficType));
```

## Architecture

### Component Overview

1. **SQSListener**: 
   - Listens to S3 events via SQS
   - Coordinates template processing flow
   - Handles error scenarios and retries

2. **S3Service**: 
   - Downloads template files from S3
   - Handles JSON parsing and validation
   - Manages S3 interactions

3. **TemplateTransformer**: 
   - Converts CMS format to domain model
   - Extracts and validates template parameters
   - Handles template versioning

4. **DynamoDBService**: 
   - Manages template persistence
   - Implements access patterns
   - Handles concurrent updates

### Flow Diagram
```
S3 Event → SQS Queue → SQSListener → S3Service → TemplateTransformer → DynamoDBService
```

## Troubleshooting

### Common Issues

1. LocalStack Connection Issues:
```bash
# Check LocalStack status
docker-compose ps
docker-compose logs localstack
```

2. Reset LocalStack:
```bash
docker-compose down -v
docker-compose up -d
```

3. Verify AWS Configuration:
```bash
aws --endpoint-url=http://localhost:4566 configure list
```

### Logging

- Application logs: `logs/application.log`
- LocalStack logs: `docker-compose logs localstack`

### Debug Mode

To enable debug logging, add the following to `application.properties`:
```properties
logging.level.com.template=DEBUG
logging.level.com.amazonaws=DEBUG
logging.level.org.springframework.cloud.aws=DEBUG
```

## Scripts

The project includes helpful scripts in the `localstack/scripts/` directory:

1. `setup.sh`: Initializes the development environment
2. `run-tests.sh`: Runs tests and generates coverage reports
3. `init.sh`: Initializes LocalStack with required AWS resources
4. `test-template-updates.sh`: Demonstrates template updates

To use the scripts:
```bash
chmod +x localstack/scripts/*.sh
./localstack/scripts/setup.sh    # Setup environment
./localstack/scripts/init.sh     # Initialize AWS resources
./localstack/scripts/run-tests.sh # Run tests
./localstack/scripts/test-template-updates.sh # Test template updates
```

