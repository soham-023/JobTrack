#!/bin/bash
set -e

# Detect Java 17 home if available
if [ -d "/opt/homebrew/opt/openjdk@17" ]; then
    export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
    export PATH="$JAVA_HOME/bin:$PATH"
fi

echo "======================================================"
echo "🚀 Starting JobTrack Backend (Spring Boot 3.2)"
echo "   Java Version: $(java -version 2>&1 | head -n 1)"
echo "   Port: http://localhost:8080"
echo "   H2 Console: http://localhost:8080/h2-console"
echo "   Default Login: demo@jobtrack.com / password123"
echo "======================================================"

mvn spring-boot:run -o
