#!/bin/bash

# Set AWS local endpoint
export AWS_ENDPOINT="http://localhost:4566"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}Testing template update functionality...${NC}"

# Function to check if LocalStack is running
check_localstack() {
    if ! curl -s "$AWS_ENDPOINT" > /dev/null; then
        echo -e "${RED}Error: LocalStack is not running. Please start it first.${NC}"
        exit 1
    fi
}

# Function to wait for SQS message processing
wait_for_processing() {
    echo "Waiting for message processing..."
    sleep 3
}

# Create test template V1 (based on template.json)
cat > template_v1.json << EOF
{
  "sys": { "type": "Array" },
  "total": 1,
  "skip": 0,
  "limit": 1000,
  "items": [
    {
      "sys": {
        "id": "111-111-111",
        "type": "Entry"
      },
      "fields": {
        "key": "TRAIN_DELAYED",
        "name": "Train Delayed V1",
        "trafficType": {
          "sys": {
            "type": "Link",
            "linkType": "Entry",
            "id": "222-222-222"
          }
        },
        "subject": {
          "nodeType": "document",
          "content": [
            {
              "nodeType": "paragraph",
              "content": [
                {
                  "nodeType": "text",
                  "value": "Train "
                },
                {
                  "nodeType": "embedded-entry-inline",
                  "data": {
                    "target": {
                      "sys": {
                        "id": "333-333-333"
                      }
                    }
                  }
                },
                {
                  "nodeType": "text",
                  "value": " is delayed"
                }
              ]
            }
          ]
        },
        "body": {
          "nodeType": "document",
          "content": [
            {
              "nodeType": "paragraph",
              "content": [
                {
                  "nodeType": "text",
                  "value": "Train "
                },
                {
                  "nodeType": "embedded-entry-inline",
                  "data": {
                    "target": {
                      "sys": {
                        "id": "333-333-333"
                      }
                    }
                  }
                },
                {
                  "nodeType": "text",
                  "value": " is delayed by 10 minutes."
                }
              ]
            }
          ]
        }
      }
    }
  ],
  "includes": {
    "Entry": [
      {
        "sys": {
          "id": "222-222-222"
        },
        "fields": {
          "key": "LONG_DISTANCE",
          "name": "Long Distance"
        }
      },
      {
        "sys": {
          "id": "333-333-333"
        },
        "fields": {
          "key": "TRAIN_TYPE",
          "name": "Train Type"
        }
      }
    ]
  }
}
EOF

# Create test template V2 (updated version)
cat > template_v2.json << EOF
{
  "sys": { "type": "Array" },
  "total": 1,
  "skip": 0,
  "limit": 1000,
  "items": [
    {
      "sys": {
        "id": "111-111-111",
        "type": "Entry"
      },
      "fields": {
        "key": "TRAIN_DELAYED",
        "name": "Train Delayed V2",
        "trafficType": {
          "sys": {
            "type": "Link",
            "linkType": "Entry",
            "id": "222-222-222"
          }
        },
        "subject": {
          "nodeType": "document",
          "content": [
            {
              "nodeType": "paragraph",
              "content": [
                {
                  "nodeType": "text",
                  "value": "Updated: Train "
                },
                {
                  "nodeType": "embedded-entry-inline",
                  "data": {
                    "target": {
                      "sys": {
                        "id": "333-333-333"
                      }
                    }
                  }
                },
                {
                  "nodeType": "text",
                  "value": " delay notice"
                }
              ]
            }
          ]
        },
        "body": {
          "nodeType": "document",
          "content": [
            {
              "nodeType": "paragraph",
              "content": [
                {
                  "nodeType": "text",
                  "value": "Updated: Train "
                },
                {
                  "nodeType": "embedded-entry-inline",
                  "data": {
                    "target": {
                      "sys": {
                        "id": "333-333-333"
                      }
                    }
                  }
                },
                {
                  "nodeType": "text",
                  "value": " is now delayed by 20 minutes."
                }
              ]
            }
          ]
        }
      }
    }
  ],
  "includes": {
    "Entry": [
      {
        "sys": {
          "id": "222-222-222"
        },
        "fields": {
          "key": "LONG_DISTANCE",
          "name": "Long Distance"
        }
      },
      {
        "sys": {
          "id": "333-333-333"
        },
        "fields": {
          "key": "TRAIN_TYPE",
          "name": "Train Type"
        }
      }
    ]
  }
}
EOF

# Check LocalStack
check_localstack

echo -e "\n${BLUE}1. Uploading template V1...${NC}"
aws --endpoint-url=$AWS_ENDPOINT s3 cp \
    template_v1.json \
    s3://s3-bucket/templates/train_delayed_v1.json

wait_for_processing

echo -e "\n${BLUE}2. Checking DynamoDB for V1...${NC}"
aws --endpoint-url=$AWS_ENDPOINT dynamodb scan \
    --table-name dynamodb-table

echo -e "\n${BLUE}3. Uploading template V2 (same ID)...${NC}"
aws --endpoint-url=$AWS_ENDPOINT s3 cp \
    template_v2.json \
    s3://s3-bucket/templates/train_delayed_v2.json

wait_for_processing

echo -e "\n${BLUE}4. Checking DynamoDB for V2 (should show updated content)...${NC}"
aws --endpoint-url=$AWS_ENDPOINT dynamodb scan \
    --table-name dynamodb-table

# Cleanup
rm template_v1.json template_v2.json

echo -e "\n${GREEN}Test completed!${NC}"
echo "Check the DynamoDB outputs above to verify that:"
echo "1. The template was initially created with 10 minute delay"
echo "2. The template was updated with 20 minute delay (same ID: 111-111-111)" 