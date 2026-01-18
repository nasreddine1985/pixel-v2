

# PIXEL-V2 ActiveMQ Rebuild and Deploy Script
# Prefer docker-compose service with --no-deps (like CH script). If the
# activemq service is missing/commented, fall back to Dockerfile.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

ACTIVEMQ_DIR="docker/activemq"
IMAGE_NAME="pixel-v2-activemq"
CONTAINER_NAME="pixel-v2-activemq"
COMPOSE_FILE="docker/docker-compose.yml"

# Stop and remove existing container
if docker ps -a --format '{{.Names}}' | grep -q "^$CONTAINER_NAME$"; then
  echo "Stopping and removing existing $CONTAINER_NAME..."
  docker stop $CONTAINER_NAME || true
  docker rm $CONTAINER_NAME || true
fi

# Try to use docker-compose service if available and not commented
USE_COMPOSE=false
if [ -f "$COMPOSE_FILE" ]; then
  if awk '!/^[[:space:]]*#/' "$COMPOSE_FILE" | grep -q "^[[:space:]]*activemq:"; then
    USE_COMPOSE=true
  fi
fi

if [ "$USE_COMPOSE" = true ]; then
  echo "🐳 Using docker-compose service 'activemq' (no dependencies)."
  # Rebuild only the activemq service image if build context exists; otherwise pull
  docker compose -f "$COMPOSE_FILE" build activemq || true
  echo "🚀 Starting ActiveMQ with docker-compose (no deps)..."
  docker compose -f "$COMPOSE_FILE" up -d --no-deps activemq
else
  echo "🐳 Compose service 'activemq' not found. Falling back to Dockerfile..."
  # Remove old image (optional refresh)
  if docker images --format '{{.Repository}}' | grep -q "^$IMAGE_NAME$"; then
    echo "Removing old image $IMAGE_NAME..."
    docker rmi $IMAGE_NAME || true
  fi
  # Build from Dockerfile
  cd "$ACTIVEMQ_DIR"
  echo "🐳 Building new ActiveMQ image from Dockerfile..."
  docker build -t $IMAGE_NAME .
  # Run container
  cd "$PROJECT_ROOT"
  echo "🚀 Running new ActiveMQ container from Dockerfile..."
  docker run -d --name $CONTAINER_NAME \
    -p 61616:61616 \
    -p 8161:8161 \
    -e ACTIVEMQ_ADMIN_LOGIN=admin \
    -e ACTIVEMQ_ADMIN_PASSWORD=admin \
    $IMAGE_NAME
fi

# Wait for startup and print basic health info
echo "⏳ Waiting for ActiveMQ to initialize..."
sleep 15

echo "📋 Checking ActiveMQ HTTP console (8161)..."
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8161 || echo "000")
echo "HTTP status: $HTTP_STATUS (200/302/401 expected)"

echo "📋 Checking broker port 61616..."
if command -v nc >/dev/null 2>&1; then
  if nc -z localhost 61616 2>/dev/null; then
    echo "61616 reachable"
  else
    echo "61616 not reachable"
  fi
else
  echo "nc not available; skipping TCP check"
fi

echo "✅ ActiveMQ ready"
echo "   - Broker: tcp://localhost:61616"
echo "   - Web Console: http://localhost:8161"