#!/bin/sh
docker run -d --name kinesis-localstack -e SERVICES=kinesis,dynamodb,cloudwatch -p 4566:4566 -e KINESIS_PROVIDER=kinesis-mock -e KINESIS_INITIALIZE_STREAMS=cloudevents-stream localstack/localstack:1.1
