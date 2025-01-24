#!/bin/bash

# Run unit tests
echo "Running unit tests..."
./mvnw test

# Generate coverage report
echo "Generating coverage report..."
./mvnw jacoco:report

echo "Tests complete! Check coverage report at target/site/jacoco/index.html" 