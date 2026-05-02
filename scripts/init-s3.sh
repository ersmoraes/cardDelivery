#!/bin/bash
set -e

awslocal s3 mb s3://carddelivery-audit --region us-east-1
echo "Bucket carddelivery-audit criado com sucesso."