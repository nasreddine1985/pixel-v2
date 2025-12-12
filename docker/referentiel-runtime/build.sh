#!/bin/bash
# Build script for referentiel runtime Docker image

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
IMAGE_NAME="pixel-v2/referentiel-runtime"
IMAGE_TAG="latest"
REGISTRY=""
PUSH_TO_REGISTRY=false
BUILD_CONTEXT="../"
DOCKERFILE_PATH="docker/referentiel-runtime/Dockerfile"

# Function to display usage
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo "Options:"
    echo "  -t, --tag TAG         Docker image tag (default: latest)"
    echo "  -n, --name NAME       Docker image name (default: pixel-v2/referentiel-runtime)"
    echo "  -r, --registry REG    Registry to push to (optional)"
    echo "  -p, --push           Push image to registry after build"
    echo "  -h, --help           Display this help message"
    echo ""
    echo "Examples:"
    echo "  $0                                    # Build with default settings"
    echo "  $0 -t v1.0.1                        # Build with specific tag"
    echo "  $0 -t v1.0.1 -p -r my-registry.com  # Build, tag and push to registry"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -t|--tag)
            IMAGE_TAG="$2"
            shift 2
            ;;
        -n|--name)
            IMAGE_NAME="$2"
            shift 2
            ;;
        -r|--registry)
            REGISTRY="$2"
            shift 2
            ;;
        -p|--push)
            PUSH_TO_REGISTRY=true
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            usage
            exit 1
            ;;
    esac
done

# Construct full image name
if [[ -n "$REGISTRY" ]]; then
    FULL_IMAGE_NAME="${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
else
    FULL_IMAGE_NAME="${IMAGE_NAME}:${IMAGE_TAG}"
fi

echo -e "${BLUE}🐳 Building Docker image for referentiel runtime...${NC}"
echo -e "${YELLOW}Image name: ${FULL_IMAGE_NAME}${NC}"
echo -e "${YELLOW}Build context: ${BUILD_CONTEXT}${NC}"
echo -e "${YELLOW}Dockerfile: ${DOCKERFILE_PATH}${NC}"

# Check if we're in the correct directory (should be in docker folder or root)
if [[ ! -f "${DOCKERFILE_PATH}" && ! -f "referentiel-runtime/Dockerfile" ]]; then
    echo -e "${RED}Error: Dockerfile not found${NC}"
    echo -e "${YELLOW}Please run this script from the docker directory or project root${NC}"
    echo -e "${YELLOW}Expected: ${DOCKERFILE_PATH}${NC}"
    exit 1
fi

# Adjust paths if running from docker directory
if [[ -f "referentiel-runtime/Dockerfile" ]]; then
    BUILD_CONTEXT="../"
    DOCKERFILE_PATH="referentiel-runtime/Dockerfile"
fi

# Check if build context contains the required files
if [[ ! -f "${BUILD_CONTEXT}/pom.xml" ]]; then
    echo -e "${RED}Error: Parent pom.xml not found in build context${NC}"
    echo -e "${YELLOW}Build context: ${BUILD_CONTEXT}${NC}"
    exit 1
fi

if [[ ! -d "${BUILD_CONTEXT}/referentiel" ]]; then
    echo -e "${RED}Error: referentiel directory not found in build context${NC}"
    echo -e "${YELLOW}Build context: ${BUILD_CONTEXT}${NC}"
    exit 1
fi

# Build the Docker image
echo -e "${GREEN}🔨 Building Docker image...${NC}"
docker build -f "${DOCKERFILE_PATH}" -t "${FULL_IMAGE_NAME}" "${BUILD_CONTEXT}"

if [[ $? -eq 0 ]]; then
    echo -e "${GREEN}✅ Docker image built successfully: ${FULL_IMAGE_NAME}${NC}"
else
    echo -e "${RED}❌ Docker build failed${NC}"
    exit 1
fi

# Push to registry if requested
if [[ "$PUSH_TO_REGISTRY" == true ]]; then
    if [[ -z "$REGISTRY" ]]; then
        echo -e "${RED}Error: Registry not specified for push operation${NC}"
        exit 1
    fi
    
    echo -e "${BLUE}📤 Pushing image to registry...${NC}"
    docker push "$FULL_IMAGE_NAME"
    
    if [[ $? -eq 0 ]]; then
        echo -e "${GREEN}✅ Image pushed successfully to registry${NC}"
    else
        echo -e "${RED}❌ Failed to push image to registry${NC}"
        exit 1
    fi
fi

# Display image information
echo -e "${GREEN}🎉 Image build complete!${NC}"
echo -e "${YELLOW}📊 Image details:${NC}"
docker images "$FULL_IMAGE_NAME" --format "table {{.Repository}}\t{{.Tag}}\t{{.ID}}\t{{.CreatedAt}}\t{{.Size}}"

# Show example docker run command
echo -e "${BLUE}"
echo "🚀 To run the container:"
echo "docker run -d \\"
echo "  --name referentiel-service \\"
echo "  -p 8099:8099 \\"
echo "  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/pixelv2 \\"
echo "  -e DATABASE_USERNAME=pixelv2 \\"
echo "  -e DATABASE_PASSWORD=pixelv2_secure_password \\"
echo "  ${FULL_IMAGE_NAME}"
echo ""
echo "🔗 Or with network (if using docker-compose network):"
echo "docker run -d \\"
echo "  --name referentiel-service \\"
echo "  --network pixel-v2_default \\"
echo "  -p 8099:8099 \\"
echo "  -e DATABASE_URL=jdbc:postgresql://postgresql:5432/pixelv2 \\"
echo "  -e DATABASE_USERNAME=pixelv2 \\"
echo "  -e DATABASE_PASSWORD=pixelv2_secure_password \\"
echo "  ${FULL_IMAGE_NAME}"
echo -e "${NC}"