#!/bin/bash

# Flow Application Startup Script
# Handles different profiles and database configurations

set -e

# Configuration  
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# Colors
BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

show_banner() {
    echo
    echo -e "${BLUE}"
    echo "┌─────────────────────────────────────────────────┐"
    echo "│            Flow Application Runner              │"
    echo "│         PACS Message Processing Service         │"
    echo "└─────────────────────────────────────────────────┘"
    echo -e "${NC}"
}

log() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

show_help() {
    echo -e "${GREEN}Available Commands:${NC}"
    echo
    echo "  dev         - Run with H2 in-memory database (development)"
    echo "  prod        - Run with PostgreSQL database (production)"
    echo "  test        - Run tests only"
    echo "  build       - Build the application"
    echo "  clean       - Clean and build"
    echo "  postgres    - Setup/check PostgreSQL database"
    echo "  check       - Check prerequisites and connectivity"
    echo "  help        - Show this help"
    echo
    echo -e "${YELLOW}Database Profiles:${NC}"
    echo "  dev profile  - H2 in-memory database (no setup required)"
    echo "  prod profile - PostgreSQL database (requires setup)"
    echo
    echo -e "${YELLOW}Usage Examples:${NC}"
    echo "  ./start-flow.sh dev                 # Quick start with H2"
    echo "  ./start-flow.sh prod                # Production with PostgreSQL"
    echo "  ./start-flow.sh postgres setup      # Setup PostgreSQL first"
    echo "  ./start-flow.sh build               # Build only"
    echo
}

check_prerequisites() {
    log "Checking prerequisites..."
    
    # Check Java
    if ! command -v java &> /dev/null; then
        error "Java is not installed or not in PATH"
        return 1
    fi
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        error "Maven is not installed or not in PATH" 
        return 1
    fi
    
    # Check ActiveMQ Artemis
    if ! nc -z localhost 61616 2>/dev/null; then
        warn "ActiveMQ Artemis not accessible on localhost:61616"
        warn "Make sure Artemis is running for JMS functionality"
    else
        success "ActiveMQ Artemis is accessible"
    fi
    
    success "Prerequisites check completed"
}

build_application() {
    log "Building Flow application..."
    cd "$PROJECT_DIR"
    
    if mvn clean compile; then
        success "Build completed successfully"
        return 0
    else
        error "Build failed"
        return 1
    fi
}

run_tests() {
    log "Running tests..."
    cd "$PROJECT_DIR"
    
    if mvn test; then
        success "All tests passed"
        return 0
    else
        error "Some tests failed"
        return 1
    fi
}

run_with_h2() {
    log "Starting Flow application with H2 database (development profile)..."
    cd "$PROJECT_DIR"
    
    log "Configuration:"
    log "  Database: H2 in-memory"
    log "  Profile: dev"
    log "  JMS: ActiveMQ Artemis (localhost:61616)"
    log "  H2 Console: http://localhost:8080/h2-console"
    echo
    
    success "Starting application..."
    mvn spring-boot:run -Dspring-boot.run.profiles=dev
}

run_with_postgres() {
    log "Starting Flow application with PostgreSQL database (production profile)..."
    
    # Check if PostgreSQL is configured
    if ! "$SCRIPT_DIR/setup-postgres.sh" check &>/dev/null; then
        error "PostgreSQL is not properly configured"
        log "Run: ./start-flow.sh postgres setup"
        return 1
    fi
    
    cd "$PROJECT_DIR"
    
    log "Configuration:"
    log "  Database: PostgreSQL (localhost:5432/pixelv2)"
    log "  Profile: default (production)"  
    log "  JMS: ActiveMQ Artemis (localhost:61616)"
    echo
    
    success "Starting application..."
    mvn spring-boot:run
}

manage_postgres() {
    local action="${1:-check}"
    
    case "$action" in
        "setup")
            log "Setting up PostgreSQL for Flow application..."
            "$SCRIPT_DIR/setup-postgres.sh" setup
            ;;
        "check")
            log "Checking PostgreSQL status..."
            "$SCRIPT_DIR/setup-postgres.sh" check
            ;;
        "test")
            log "Testing PostgreSQL connection..."
            "$SCRIPT_DIR/setup-postgres.sh" test
            ;;
        "info")
            "$SCRIPT_DIR/setup-postgres.sh" info
            ;;
        *)
            error "Unknown PostgreSQL action: $action"
            log "Available actions: setup, check, test, info"
            return 1
            ;;
    esac
}

main() {
    show_banner
    
    case "${1:-help}" in
        "dev"|"development")
            check_prerequisites
            run_with_h2
            ;;
            
        "prod"|"production")
            check_prerequisites  
            run_with_postgres
            ;;
            
        "test")
            check_prerequisites
            run_tests
            ;;
            
        "build")
            check_prerequisites
            build_application
            ;;
            
        "clean")
            log "Cleaning and building..."
            cd "$PROJECT_DIR"
            mvn clean package
            ;;
            
        "postgres")
            manage_postgres "${2:-check}"
            ;;
            
        "check")
            check_prerequisites
            "$SCRIPT_DIR/setup-postgres.sh" check 2>/dev/null || warn "PostgreSQL not configured"
            "$SCRIPT_DIR/test-setup.sh"
            ;;
            
        "help"|"-h"|"--help"|"")
            show_help
            ;;
            
        *)
            error "Unknown command: $1"
            echo
            show_help
            exit 1
            ;;
    esac
}

main "$@"