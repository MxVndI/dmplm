#!/bin/bash

echo "🔨 DiplomShop Build Verification"
echo "=================================="
echo ""

cd /sessions/nice-festive-cray/mnt/BIGGEST

# Check if pom.xml exists
if [ ! -f "pom.xml" ]; then
    echo "❌ Root pom.xml not found"
    exit 1
fi

echo "✅ Parent POM found"

# Check all 5 services
services=("diplom-shop" "diplom-test-service" "diplom-selector-service" "diplom-demographic-service" "diplom-notification-service")

for service in "${services[@]}"; do
    if [ -d "$service" ] && [ -f "$service/pom.xml" ]; then
        echo "✅ $service/pom.xml found"
    else
        echo "❌ $service/pom.xml NOT found"
        exit 1
    fi
done

echo ""
echo "📦 Building all services (this may take 5-10 minutes)..."
echo ""

# Build with Maven
mvn clean package -DskipTests 2>&1 | tail -50

# Check if build was successful
if [ ${PIPESTATUS[0]} -eq 0 ]; then
    echo ""
    echo "✅ BUILD SUCCESSFUL!"
    echo ""
    echo "Generated JARs:"
    find . -name "*.jar" -path "*/target/*" | grep -E "(diplom-shop|diplom-test-service|diplom-selector-service|diplom-demographic-service|diplom-notification-service)" | sort
    echo ""
    echo "🎉 All services compiled successfully!"
else
    echo ""
    echo "❌ BUILD FAILED"
    echo ""
    echo "Troubleshooting:"
    echo "1. Check Java version: java -version (should be 21)"
    echo "2. Check Maven: mvn -version (should be 3.9+)"
    echo "3. Check internet connection"
    echo "4. Run: mvn clean install -DskipTests (to download dependencies)"
    exit 1
fi
