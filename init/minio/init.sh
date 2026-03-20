#!/bin/sh
set -e

echo "Starting MinIO initialization..."

mc alias set local http://photo-storage:9000 "${MINIO_AKEY}" "${MINIO_SKEY}"

mc mb --ignore-existing local/students

mc cp /tmp/photos/*.png local/students/ || echo "No photos found to upload"

echo "MinIO initialized: bucket created, photos uploaded"