#!/usr/bin/env bash
# Start the JobTrack backend locally with Maven.
set -euo pipefail

usage() {
    cat <<USAGE
Usage: ./run.sh [--offline] [--help]

Starts the JobTrack Spring Boot backend on http://localhost:8080.

Options:
  -o, --offline   Run Maven in offline mode (requires dependencies to be cached already)
  -h, --help      Show this help message and exit
USAGE
}

MVN_ARGS=()
while [[ $# -gt 0 ]]; do
    case "$1" in
        -o|--offline) MVN_ARGS+=("-o") ;;
        -h|--help) usage; exit 0 ;;
        *) echo "Unknown option: $1" >&2; usage >&2; exit 1 ;;
    esac
    shift
done

# Always run from the project root so the script works from any directory.
cd "$(dirname "$0")"

# Detect Java 17 home if available (Homebrew on macOS)
if [[ -d "/opt/homebrew/opt/openjdk@17" ]]; then
    export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
    export PATH="$JAVA_HOME/bin:$PATH"
fi

for cmd in java mvn; do
    if ! command -v "$cmd" >/dev/null 2>&1; then
        echo "Error: '$cmd' was not found on PATH. Install Java 17+ and Maven 3.8+ first." >&2
        exit 1
    fi
done

echo "======================================================"
echo "🚀 Starting JobTrack Backend (Spring Boot 3.2)"
echo "   Java Version: $(java -version 2>&1 | head -n 1)"
echo "   Port: http://localhost:8080"
echo "   H2 Console: http://localhost:8080/h2-console"
echo "   Default Login: demo@jobtrack.com / password123"
echo "======================================================"

exec mvn "${MVN_ARGS[@]+"${MVN_ARGS[@]}"}" spring-boot:run
