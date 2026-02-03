#!/usr/bin/env bash

# Distribution Test Script
# Tests both JAR and Docker distributions

APP_NAME="pfxprobe"
APP_VERSION="1.0"
DOCKER_IMAGE="${APP_NAME}:${APP_VERSION}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
JAR_PATH="${PROJECT_DIR}/target/${APP_NAME}-${APP_VERSION}.jar"
FIXTURES_DIR="${PROJECT_DIR}/fixtures"

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
	if echo "$output" | grep -q "$expected"; then
		echo "✓ ${name} passed"
	else
		echo "✗ ${name} failed"
		FAILED=1
	fi
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
	"Started"

# Test Docker
echo "--- Docker Tests ---"
echo ""

run_test "Docker Help" \
	"${CONTAINER_CMD} run --rm ${DOCKER_IMAGE} ${APP_NAME}" \
	"usage"

run_test "Docker Scan" \
	"${CONTAINER_CMD} run --rm -v ${FIXTURES_DIR}:/fixtures ${DOCKER_IMAGE} ${APP_NAME} -dir /fixtures" \
	"Started"

# Final result
if [ $FAILED -eq 0 ]; then
	echo "✓ All distribution tests passed!"
	exit 0
else
	echo "✗ Some tests failed"
	exit 1
fi
