#!/usr/bin/env bash
# Lance les 4 microservices Node en arrière-plan (Linux / macOS / Git Bash). Ctrl+C pour tout arrêter.
cd "$(dirname "$0")/../backend" || exit 1
for s in session-service:8087 trust-service:8088 notification-service:8089 ai-service:8090; do
  d=${s%%:*}; p=${s##*:}
  [ -d "$d/node_modules" ] || (cd "$d" && npm install)
  (cd "$d" && PORT=$p npm start) &
done
trap 'kill 0' INT TERM; wait
