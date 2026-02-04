#!/usr/bin/env bash

# Distribution Test Script
# Tests both JAR and Docker distributions

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
POM_FILE="${PROJECT_DIR}/pom.xml"

# Read version and artifactId from pom.xml
APP_NAME=$(grep -oP '(?<=<artifactId>)[^<]+' "$POM_FILE" | head -1)
APP_VERSION=$(grep -oP '(?<=<version>)[^<]+' "$POM_FILE" | head -1)

DOCKER_IMAGE="${APP_NAME}:${APP_VERSION}"
JAR_PATH="${PROJECT_DIR}/build/result/repo/${APP_NAME}-${APP_VERSION}.jar"
FIXTURES_DIR="${PROJECT_DIR}/fixtures"

echo "Testing ${APP_NAME} version ${APP_VERSION}..."

# Use podman or docker
CONTAINER_CMD="docker"
if command -v podman &>/dev/null; then
  CONTAINER_CMD="podman"
fi

FAILED=0

run_test() {
  local name="$1"
  local command="$2"
  local expected="$3"

  echo "=== ${name} ==="
  local output
  output=$(eval "$command" 2>&1)
  echo "$output"

  # Check for expected pattern
  if ! echo "$output" | grep -q "$expected"; then
    echo "✗ ${name} failed - expected pattern not found: $expected"
    FAILED=1
    return
  fi

  # Check for any ERRORs
  if echo "$output" | grep -q "ERROR:"; then
    echo "✗ ${name} failed - found ERROR in output"
    FAILED=1
    return
  fi

  echo "✓ ${name} passed"
  echo ""
}

echo "Testing distributions..."
echo ""

# Test JAR
echo "--- JAR Tests ---"
echo ""

run_test "JAR Help" \
  "java -jar ${JAR_PATH}" \
  "usage"

run_test "JAR Scan" \
  "java -jar ${JAR_PATH} -dir ${FIXTURES_DIR}" \
  "Finished"

# Test Docker
echo "--- Docker Tests ---"
echo ""

run_test "Docker Help" \
  "${CONTAINER_CMD} run --rm ${DOCKER_IMAGE} ${APP_NAME}" \
  "usage"

run_test "Docker Scan" \
  "${CONTAINER_CMD} run --rm -v ${FIXTURES_DIR}:/fixtures ${DOCKER_IMAGE} ${APP_NAME} -dir /fixtures" \
  "Finished"

run_test "Docker Scan - SH Entrypoint" \
  "${CONTAINER_CMD} run --rm -v ${FIXTURES_DIR}:/fixtures ${DOCKER_IMAGE} sh -c 'pfxprobe -dir /fixtures'" \
  "Finished"

# Final result
if [ $FAILED -eq 0 ]; then
  echo "✓ All distribution tests passed!"
  exit 0
else
  echo "✗ Some tests failed"
  exit 1
fi
