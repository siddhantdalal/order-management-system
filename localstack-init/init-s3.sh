#!/bin/bash
awslocal s3 mb s3://product-images
awslocal s3api put-bucket-cors --bucket product-images --cors-configuration '{"CORSRules":[{"AllowedOrigins":["*"],"AllowedMethods":["GET","PUT","POST"],"AllowedHeaders":["*"]}]}'
echo "S3 bucket 'product-images' created with CORS configuration"
