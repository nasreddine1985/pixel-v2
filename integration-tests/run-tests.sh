#!/bin/bash

# Integration Tests Runner
# Execute integration tests for PIXEL-V2 modules

echo "🧪 PIXEL-V2 Integration Tests Runner"
echo "===================================="

# Check Maven installation
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found. Please install Maven first."
    exit 1
fi

# Change to integration tests directory
cd "$(dirname "$0")"

echo "📋 Test Options:"
echo "  1. Run unit tests only (mock-based)"
echo "  2. Run integration tests only (full pipeline)"
echo "  3. Run all tests (unit + integration)"
echo "  4. Run specific test class"
echo ""

# Default to all tests if no argument provided
TEST_TYPE=${1:-3}

case $TEST_TYPE in
    1)
        echo "🔄 Running unit tests..."
        mvn test -Dtest="*Test" -Dskip.integration.tests=true
        ;;
    2)
        echo "🔄 Running integration tests..."
        mvn verify -Dskip.unit.tests=true
        ;;
    3)
        echo "🔄 Running all tests..."
        mvn verify
        ;;
    4)
        if [ -z "$2" ]; then
            echo "❌ Please specify test class name"
            echo "   Example: ./run-tests.sh 4 Pacs008FlowIntegrationTest"
            exit 1
        fi
        echo "🔄 Running specific test: $2"
        mvn test -Dtest="$2"
        ;;
    *)
        echo "❌ Invalid option: $TEST_TYPE"
        echo "   Use: 1 (unit), 2 (integration), 3 (all), or 4 (specific)"
        exit 1
        ;;
esac

TEST_EXIT_CODE=$?

echo ""
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo "✅ Tests completed successfully!"
else
    echo "❌ Tests failed with exit code: $TEST_EXIT_CODE"
    echo ""
    echo "📝 Check test reports:"
    echo "   - Unit tests: target/surefire-reports/"
    echo "   - Integration tests: target/failsafe-reports/"
fi

exit $TEST_EXIT_CODE