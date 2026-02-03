#!/usr/bin/env bash
# wait-for-it.sh - Wait for a service to be available before starting
# Usage: wait-for-it.sh host:port [-t timeout] [-- command args]

set -e

TIMEOUT=60
QUIET=0
HOST=""
PORT=""

usage() {
    cat << USAGE >&2
Usage:
    $0 host:port [-t timeout] [-- command args]
    -t TIMEOUT                  Timeout in seconds, zero for no timeout
    -q                          Don't output any status messages
    -- COMMAND ARGS             Execute command with args after the test finishes
USAGE
    exit 1
}

wait_for() {
    if [ "$TIMEOUT" -gt 0 ]; then
        echo "Waiting for $HOST:$PORT for up to $TIMEOUT seconds..."
    else
        echo "Waiting for $HOST:$PORT without a timeout..."
    fi

    START_TIME=$(date +%s)

    while true; do
        if nc -z "$HOST" "$PORT" > /dev/null 2>&1; then
            END_TIME=$(date +%s)
            echo "$HOST:$PORT is available after $((END_TIME - START_TIME)) seconds"
            break
        fi

        if [ "$TIMEOUT" -gt 0 ]; then
            ELAPSED=$(($(date +%s) - START_TIME))
            if [ "$ELAPSED" -ge "$TIMEOUT" ]; then
                echo "Timeout reached waiting for $HOST:$PORT" >&2
                exit 1
            fi
        fi

        sleep 1
    done
}

while [ $# -gt 0 ]; do
    case "$1" in
        *:* )
        HOST=$(echo "$1" | cut -d: -f1)
        PORT=$(echo "$1" | cut -d: -f2)
        shift
        ;;
        -q | --quiet)
        QUIET=1
        shift
        ;;
        -t)
        TIMEOUT="$2"
        if [ "$TIMEOUT" = "" ]; then break; fi
        shift 2
        ;;
        --)
        shift
        exec "$@"
        ;;
        *)
        echo "Unknown argument: $1"
        usage
        ;;
    esac
done

if [ "$HOST" = "" ] || [ "$PORT" = "" ]; then
    echo "Error: you need to provide a host and port to test."
    usage
fi

wait_for

exit 0
