#!/bin/bash

# PACS.008 Message Injection Runner
# Convenient wrapper for all injection scripts

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Colors
BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

show_banner() {
    echo
    echo -e "${BLUE}"
    echo "┌─────────────────────────────────────────────────┐"
    echo "│           PACS.008 Message Injector             │"
    echo "│              ActiveMQ Artemis                   │"
    echo "└─────────────────────────────────────────────────┘"
    echo -e "${NC}"
}

show_menu() {
    echo -e "${GREEN}Available Commands:${NC}"
    echo
    echo "  test        - Validate setup and connectivity"
    echo "  quick       - Send one test message"
    echo "  batch       - Send multiple test messages"  
    echo "  samples     - Send all sample PACS.008 messages"
    echo "  check       - Check broker status only"
    echo "  help        - Show detailed help"
    echo
    echo -e "${YELLOW}Usage Examples:${NC}"
    echo "  ./run.sh test                    # Test setup"
    echo "  ./run.sh quick                   # One message"
    echo "  ./run.sh batch 5                 # 5 messages"
    echo "  ./run.sh samples --delay 3       # All samples, 3s delay"
    echo
}

main() {
    show_banner
    
    case "${1:-help}" in
        "test"|"validate"|"setup")
            echo -e "${GREEN}Running setup validation...${NC}"
            echo
            "$SCRIPT_DIR/test-setup.sh"
            ;;
            
        "quick"|"single")
            echo -e "${GREEN}Sending quick test message...${NC}"
            echo
            "$SCRIPT_DIR/quick-inject.sh" single
            ;;
            
        "batch"|"multiple")
            local count=${2:-3}
            echo -e "${GREEN}Sending $count test messages...${NC}"
            echo
            "$SCRIPT_DIR/quick-inject.sh" multiple "$count" "${3:-2}"
            ;;
            
        "samples"|"all")
            echo -e "${GREEN}Sending all PACS.008 sample messages...${NC}"
            echo
            shift  # Remove first argument
            "$SCRIPT_DIR/inject-pacs008-messages.sh" "$@"
            ;;
            
        "check"|"status")
            echo -e "${GREEN}Checking broker status...${NC}"
            echo
            "$SCRIPT_DIR/quick-inject.sh" check
            ;;
            
        "help"|"-h"|"--help"|"")
            show_menu
            echo
            echo -e "${YELLOW}For detailed help on specific scripts:${NC}"
            echo "  ./inject-pacs008-messages.sh --help"
            echo "  ./quick-inject.sh help"
            echo
            ;;
            
        *)
            echo -e "${YELLOW}Unknown command: $1${NC}"
            echo
            show_menu
            exit 1
            ;;
    esac
}

main "$@"