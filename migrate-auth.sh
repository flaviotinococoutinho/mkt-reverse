#!/bin/bash
# Migration script for Auth to package-by-feature

set -e
cd /workspace/mkt-reverse

echo "=== Creating directory structure ==="
mkdir -p features/auth/domain/model
mkdir -p features/auth/domain/valueobject
mkdir -p features/auth/domain/repository
mkdir -p features/auth/domain/event
mkdir -p features/auth/infrastructure/persistence
mkdir -p features/auth/infrastructure/security
mkdir -p features/auth/api

echo "=== Copying domain files ==="
cp -r modules/user-management/src/main/java/com/marketplace/user/domain/model/* features/auth/domain/model/ 2>/dev/null || true
cp -r modules/user-management/src/main/java/com/marketplace/user/domain/valueobject/* features/auth/domain/valueobject/ 2>/dev/null || true
cp -r modules/user-management/src/main/java/com/marketplace/user/domain/repository/* features/auth/domain/repository/ 2>/dev/null || true
cp -r modules/user-management/src/main/java/com/marketplace/user/domain/event/* features/auth/domain/event/ 2>/dev/null || true

echo "=== Copying infrastructure files ==="
cp -r modules/user-management/src/main/java/com/marketplace/user/infrastructure/persistence/* features/auth/infrastructure/persistence/ 2>/dev/null || true

echo "=== Copying security files ==="
cp application/api-gateway/src/main/java/com/marketplace/gateway/security/JwtAuthenticationFilter.java features/auth/infrastructure/security/ 2>/dev/null || true

echo "=== Copying API files ==="
cp application/api-gateway/src/main/java/com/marketplace/gateway/api/AuthController.java features/auth/api/ 2>/dev/null || true
cp application/api-gateway/src/main/java/com/marketplace/gateway/api/schema/AuthSchema.java features/auth/api/ 2>/dev/null || true

echo "=== Migration complete ==="
find features/auth -name "*.java" | wc -l
