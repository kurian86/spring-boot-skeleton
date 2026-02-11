#!/bin/bash

set -e

IMAGE_NAME="es.bdo/skeleton:0.0.1"

function build_jvm() {
    echo "🏗️  Building JVM Cloud Native Image..."
    ./gradlew clean bootBuildImage
    echo "✅ Image built: $IMAGE_NAME"
}

function build_native() {
    echo "🏗️  Building Native Cloud Native Image (this may take 5-10 minutes)..."
    ./gradlew clean bootBuildImage -PnativeImage=true
    echo "✅ Native image built: $IMAGE_NAME"
}

function run_local() {
    echo "🚀 Running image locally..."
    docker run -d \
        --name skeleton-app \
        -p 8080:8080 \
        -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/catalog \
        -e SPRING_DATASOURCE_USERNAME=user \
        -e SPRING_DATASOURCE_PASSWORD=password \
        $IMAGE_NAME
    
    echo "✅ Container started!"
    echo "📊 Logs: docker logs -f skeleton-app"
    echo "🌐 API: http://localhost:8080"
}

function stop_local() {
    echo "🛑 Stopping container..."
    docker stop skeleton-app 2>/dev/null || true
    docker rm skeleton-app 2>/dev/null || true
    echo "✅ Container stopped"
}

function compose_up() {
    local compose_file="${1:-compose.yml}"
    echo "🚀 Starting services with docker compose ($compose_file)..."
    docker compose -f "$compose_file" up -d
    echo "✅ Services started!"
    echo "📊 Logs: docker compose -f $compose_file logs -f"
}

function compose_down() {
    local compose_file="${1:-compose.yml}"
    echo "🛑 Stopping docker compose services ($compose_file)..."
    docker compose -f "$compose_file" down
    echo "✅ Services stopped"
}

function show_help() {
    cat << EOF
🐳 Spring Boot Skeleton - Docker Helper

Usage: ./docker.sh [command] [options]

Commands:
  build-jvm                 Build JVM Cloud Native Image (~1 min)
  build-native              Build Native Cloud Native Image (~5-10 min)
  run                       Run the image locally
  stop                      Stop the local container
  compose-up [file]         Start all services with docker compose (default: compose.yml)
  compose-down [file]       Stop docker compose services (default: compose.yml)
  help                      Show this help message

Examples:
  ./docker.sh build-jvm
  ./docker.sh run
  ./docker.sh compose-up
  ./docker.sh compose-up compose.full.yml
  ./docker.sh compose-down compose.full.yml
EOF
}

# Main command handler
CMD="$1"
shift || true  # Remove first argument, shift returns 1 if no more args, so || true

case "$CMD" in
    build-jvm)
        build_jvm
        ;;
    build-native)
        build_native
        ;;
    run)
        run_local
        ;;
    stop)
        stop_local
        ;;
    compose-up)
        compose_up "$1"
        ;;
    compose-down)
        compose_down "$1"
        ;;
    help|--help|-h|"")
        show_help
        ;;
    *)
        echo "❌ Unknown command: $CMD"
        show_help
        exit 1
        ;;
esac
