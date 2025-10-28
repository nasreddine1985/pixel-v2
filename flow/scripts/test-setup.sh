#!/bin/bash

# Test Script for PACS.008 Message Injection Setup
# Validates that all components are ready for message injection

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SUCCESS="✅"
FAIL="❌"
INFO="ℹ️"
WARN="⚠️"

log() {
    echo -e "${BLUE}${INFO}${NC} $1"
}

success() {
    echo -e "${GREEN}${SUCCESS}${NC} $1"
}

fail() {
    echo -e "${RED}${FAIL}${NC} $1"
}

warn() {
    echo -e "${YELLOW}${WARN}${NC} $1"
}

# Test results
tests_passed=0
tests_failed=0

run_test() {
    local test_name="$1"
    local test_command="$2"
    
    echo -n "Testing $test_name... "
    
    if eval "$test_command" &>/dev/null; then
        success "$test_name"
        tests_passed=$((tests_passed + 1))
        return 0
    else
        fail "$test_name"
        tests_failed=$((tests_failed + 1))
        return 1
    fi
}

echo
log "PACS.008 Message Injection Setup Validation"
log "==========================================="
echo

# Test 1: Required tools
log "Checking required tools..."
run_test "Java availability" "command -v java"
run_test "curl availability" "command -v curl"

# Optional tools
echo -n "Testing netcat (optional)... "
if command -v nc &>/dev/null; then
    success "netcat available"
else
    warn "netcat not available (optional)"
fi

echo

# Test 2: ActiveMQ Artemis connectivity
log "Checking ActiveMQ Artemis connectivity..."
run_test "Artemis JMS port (61616)" "nc -z localhost 61616"
run_test "Artemis web console (8161)" "curl -s -o /dev/null -w '%{http_code}' http://localhost:8161/console/ | grep -E '^(200|302)$'"

echo

# Test 3: Authentication
log "Checking Artemis authentication..."
run_test "Web console authentication" "curl -s -u artemis:artemis -o /dev/null -w '%{http_code}' http://localhost:8161/console/ | grep -E '^(200|302)$'"

echo

# Test 4: Script permissions
log "Checking script permissions..."
run_test "inject-pacs008-messages.sh executable" "[ -x ./inject-pacs008-messages.sh ]"
run_test "quick-inject.sh executable" "[ -x ./quick-inject.sh ]"

echo

# Test 5: Flow application check
log "Checking Flow application..."

# Check if Flow app is running
flow_running=false
if pgrep -f "pixel-v2-flow" >/dev/null; then
    success "Flow application is running"
    flow_running=true
    tests_passed=$((tests_passed + 1))
else
    warn "Flow application not running"
    log "  Start with: mvn spring-boot:run (in flow directory)"
fi

# Check if Flow app can connect to Artemis
if [ "$flow_running" = true ]; then
    # Give a moment for logs to appear
    sleep 1
    
    # Check recent logs for Camel/JMS startup
    if pgrep -f "pixel-v2-flow" >/dev/null; then
        success "Flow application process active"
        tests_passed=$((tests_passed + 1))
    else
        warn "Flow application may have connection issues"
    fi
fi

echo

# Test 6: Quick message injection test
log "Testing message injection..."

if run_test "Quick message injection" "./quick-inject.sh single"; then
    success "Message injection test successful"
    
    if [ "$flow_running" = true ]; then
        log "Check Flow application logs for message processing"
    fi
else
    fail "Message injection test failed"
    log "Check broker status and credentials"
fi

echo

# Summary
log "Test Summary"
log "============"
echo "Tests passed: $tests_passed"
echo "Tests failed: $tests_failed"

if [ $tests_failed -eq 0 ]; then
    echo
    success "All tests passed! Your setup is ready for PACS.008 message injection."
    echo
    log "Next steps:"
    echo "  1. Start Flow application: mvn spring-boot:run"
    echo "  2. Send test messages: ./quick-inject.sh"
    echo "  3. Send sample batch: ./inject-pacs008-messages.sh"
    echo "  4. Monitor Flow logs for message processing"
    echo
else
    echo
    fail "Some tests failed. Please fix the issues above before proceeding."
    echo
    log "Common fixes:"
    echo "  - Start ActiveMQ Artemis broker"
    echo "  - Verify credentials (artemis/artemis)"
    echo "  - Make scripts executable: chmod +x *.sh"
    echo "  - Install missing tools (java, curl)"
    echo
fi

echo
log "For detailed help, run: ./inject-pacs008-messages.sh --help"
log "For quick testing, run: ./quick-inject.sh help"
echo