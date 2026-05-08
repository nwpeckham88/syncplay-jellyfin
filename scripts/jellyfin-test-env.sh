#!/usr/bin/env bash

set -euo pipefail
umask 077

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="${JELLYFIN_COMPOSE_FILE:-$ROOT_DIR/docker-compose.jellyfin.yml}"
detect_compose_cmd() {
  if [[ -n "${COMPOSE_CMD:-}" ]]; then
    printf '%s' "$COMPOSE_CMD"
    return
  fi

  if command -v docker-compose >/dev/null 2>&1; then
    printf '%s' "docker-compose"
    return
  fi

  if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
    printf '%s' "docker compose"
    return
  fi

  printf '%s' "docker-compose"
}

COMPOSE_CMD="$(detect_compose_cmd)"
SERVER_URL="${JELLYFIN_BASE_URL:-http://127.0.0.1:${JELLYFIN_PORT:-8096}}"
USERNAME="${JELLYFIN_TEST_USERNAME:-syncplaytest}"
PASSWORD="${JELLYFIN_TEST_PASSWORD:-testpass123}"
LIBRARY_NAME="${JELLYFIN_TEST_LIBRARY_NAME:-Syncplay Movies}"
CLIENT_NAME="Syncplay Jellyfin Test Harness"
CLIENT_VERSION="1.0.0"
DEVICE_NAME="Local Test Harness"
DEVICE_ID="syncplay-local-jellyfin-test"
STATE_DIR="$ROOT_DIR/.jellyfin-test"
MEDIA_ROOT="$STATE_DIR/media"
MOVIE_DIR="$MEDIA_ROOT/movies/Syncplay Test Movie (2008)"
MOVIE_FILE="$MOVIE_DIR/Syncplay Test Movie (2008).mp4"
DEFAULT_MEDIA_URL="${JELLYFIN_TEST_MEDIA_URL:-https://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4}"
DOWNLOAD_SHA256="${JELLYFIN_TEST_MEDIA_SHA256:-}"
STARTUP_TIMEOUT_SECONDS="${JELLYFIN_STARTUP_TIMEOUT_SECONDS:-180}"
SCAN_TIMEOUT_SECONDS="${JELLYFIN_SCAN_TIMEOUT_SECONDS:-180}"

cleanup() {
  rm -f "$MOVIE_FILE.tmp"
  unset ACCESS_TOKEN USER_ID || true
}

trap cleanup EXIT

read -r -a COMPOSE_ARGS <<< "$COMPOSE_CMD"

usage() {
  cat <<'USAGE'
Usage: scripts/jellyfin-test-env.sh <command>

Commands:
  up       Create media if needed, start Jellyfin, bootstrap the test server
  start    Alias for up
  seed     Create or copy the local test media only
  reset    Stop Jellyfin, remove generated config/cache, then run up
  status   Show container status and public Jellyfin info when reachable
  down     Stop and remove the Jellyfin test container
  stop     Alias for down
  logs     Show Jellyfin container logs; pass extra args after --
  help     Show this help

Environment:
  COMPOSE_CMD                Compose command override; auto-detects docker-compose, then docker compose
  JELLYFIN_IMAGE             Jellyfin image, default: jellyfin/jellyfin:10.10.7
  JELLYFIN_PORT              Host port, default: 8096
  JELLYFIN_BIND_ADDRESS      Host bind address, default: 0.0.0.0 for LAN device testing
  JELLYFIN_BASE_URL          Host-facing URL, default: http://127.0.0.1:8096
  JELLYFIN_TEST_USERNAME     Test user, default: syncplaytest
  JELLYFIN_TEST_PASSWORD     Test password, default: testpass123
  JELLYFIN_TEST_MEDIA        Optional local MP4 to copy into the test library
  JELLYFIN_TEST_MEDIA_URL    Optional MP4 URL to download when no local file is set
  JELLYFIN_TEST_MEDIA_SHA256 Optional SHA-256 checksum for downloaded media
  JELLYFIN_STARTUP_TIMEOUT_SECONDS  Server startup wait, default: 180
  JELLYFIN_SCAN_TIMEOUT_SECONDS     Library scan wait, default: 180

For physical Android phones and Android TV boxes, use the host LAN URL in the app,
for example http://192.168.1.42:8096. Android emulator can use http://10.0.2.2:8096.
USAGE
}

compose() {
  HOST_UID="${HOST_UID:-$(id -u)}" HOST_GID="${HOST_GID:-$(id -g)}" \
    "${COMPOSE_ARGS[@]}" -f "$COMPOSE_FILE" "$@"
}

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

require_tools() {
  require_command "${COMPOSE_ARGS[0]}"
  require_command curl
  require_command python3
}

ensure_dirs() {
  mkdir -p "$STATE_DIR/config" "$STATE_DIR/cache" "$MOVIE_DIR" "$ROOT_DIR/test-results/jellyfin"
  chmod -R u+rwX "$STATE_DIR"
}

json_payload() {
  python3 - "$@" <<'PY'
import json
import sys

def coerce(value: str):
    if value == "true":
        return True
    if value == "false":
        return False
    if value == "null":
        return None
    return value

args = sys.argv[1:]
if len(args) % 2 != 0:
    raise SystemExit("json_payload needs key/value pairs")

print(json.dumps({args[index]: coerce(args[index + 1]) for index in range(0, len(args), 2)}))
PY
}

json_value() {
  local path="$1"
  python3 -c '
import json
import sys

data = json.load(sys.stdin)
value = data
for part in sys.argv[1].split("."):
    if isinstance(value, dict):
        value = value.get(part)
    else:
        value = None
    if value is None:
        raise SystemExit(1)
print(value)
' "$path"
}

validate_json() {
  local response="$1"
  local label="$2"
  if ! python3 -c 'import json, sys; json.load(sys.stdin)' <<< "$response" >/dev/null 2>&1; then
    echo "Invalid JSON response from Jellyfin while reading $label" >&2
    return 1
  fi
}

urlencode() {
  python3 -c 'import sys, urllib.parse; print(urllib.parse.quote(sys.argv[1], safe=""))' "$1"
}

auth_header() {
  local token="${1:-}"
  local header="MediaBrowser Client=\"$CLIENT_NAME\", Device=\"$DEVICE_NAME\", DeviceId=\"$DEVICE_ID\", Version=\"$CLIENT_VERSION\""
  if [[ -n "$token" ]]; then
    header="$header, Token=\"$token\""
  fi
  printf '%s' "$header"
}

curl_public() {
  curl -fsS "$SERVER_URL$1"
}

curl_json() {
  local method="$1"
  local path="$2"
  local token="${3:-}"
  local body="${4:-}"
  local args=(-fsS -X "$method" "$SERVER_URL$path" -H "Authorization: $(auth_header "$token")")
  if [[ -n "$body" ]]; then
    curl "${args[@]}" -H "Content-Type: application/json" --data-binary @- <<< "$body"
    return
  fi
  curl "${args[@]}"
}

verify_media_checksum() {
  if [[ -z "$DOWNLOAD_SHA256" ]]; then
    return
  fi

  require_command sha256sum
  printf '%s  %s\n' "$DOWNLOAD_SHA256" "$MOVIE_FILE.tmp" | sha256sum -c -
}

seed_media() {
  ensure_dirs

  if [[ -n "${JELLYFIN_TEST_MEDIA:-}" ]]; then
    if [[ ! -f "$JELLYFIN_TEST_MEDIA" ]]; then
      echo "JELLYFIN_TEST_MEDIA does not exist: $JELLYFIN_TEST_MEDIA" >&2
      exit 1
    fi
    case "${JELLYFIN_TEST_MEDIA,,}" in
      *.mp4|*.m4v|*.mkv|*.mov|*.avi) ;;
      *) echo "Warning: JELLYFIN_TEST_MEDIA does not use a common video extension" >&2 ;;
    esac
    cp "$JELLYFIN_TEST_MEDIA" "$MOVIE_FILE"
    echo "Copied test media to $MOVIE_FILE"
    return
  fi

  if [[ -s "$MOVIE_FILE" ]]; then
    echo "Test media already exists at $MOVIE_FILE"
    return
  fi

  echo "Downloading free H.264/AAC test media to $MOVIE_FILE"
  curl -fL --proto '=https' --tlsv1.2 --retry 3 --connect-timeout 15 -o "$MOVIE_FILE.tmp" "$DEFAULT_MEDIA_URL"
  verify_media_checksum
  mv "$MOVIE_FILE.tmp" "$MOVIE_FILE"
}

wait_for_server() {
  local attempts=$(( (STARTUP_TIMEOUT_SECONDS + 1) / 2 ))
  echo "Waiting for Jellyfin at $SERVER_URL"
  for _ in $(seq 1 "$attempts"); do
    if curl_public "/System/Info/Public" >/dev/null 2>&1; then
      echo "Jellyfin is reachable"
      return
    fi
    sleep 2
  done

  echo "Timed out waiting for Jellyfin at $SERVER_URL" >&2
  compose ps >&2 || true
  exit 1
}

startup_completed() {
  local response
  response="$(curl_public "/System/Info/Public")"
  validate_json "$response" "public system info"
  printf '%s' "$response" | json_value "StartupWizardCompleted"
}

complete_startup() {
  local completed
  completed="$(startup_completed)"
  if [[ "$completed" == "True" || "$completed" == "true" ]]; then
    echo "Startup wizard is already complete"
    return
  fi

  echo "Completing Jellyfin startup wizard"
  curl_json POST "/Startup/Configuration" "" \
    "$(json_payload ServerName "Syncplay Test Jellyfin" UICulture "en-US" MetadataCountryCode "US" PreferredMetadataLanguage "en")" >/dev/null
  curl_json POST "/Startup/RemoteAccess" "" \
    "$(json_payload EnableRemoteAccess true)" >/dev/null
  curl_json GET "/Startup/User" >/dev/null
  curl_json POST "/Startup/User" "" \
    "$(json_payload Name "$USERNAME" Password "$PASSWORD")" >/dev/null
  curl_json POST "/Startup/Complete" >/dev/null
}

authenticate() {
  local response
  response="$(curl_json POST "/Users/AuthenticateByName" "" "$(json_payload Username "$USERNAME" Pw "$PASSWORD")")"
  validate_json "$response" "authentication"
  ACCESS_TOKEN="$(printf '%s' "$response" | json_value AccessToken)"
  USER_ID="$(printf '%s' "$response" | json_value User.Id)"
}

library_exists() {
  local libraries="$1"
  python3 -c '
import json
import sys

library_name = sys.argv[1]
libraries = json.load(sys.stdin)
raise SystemExit(0 if any(item.get("Name") == library_name for item in libraries) else 1)
' "$LIBRARY_NAME" <<< "$libraries"
}

ensure_library() {
  local libraries
  local encoded_name
  local encoded_path

  libraries="$(curl_json GET "/Library/VirtualFolders" "$ACCESS_TOKEN")"
  validate_json "$libraries" "virtual folders"
  if library_exists "$libraries"; then
    echo "Jellyfin library '$LIBRARY_NAME' already exists"
  else
    echo "Creating Jellyfin library '$LIBRARY_NAME'"
    encoded_name="$(urlencode "$LIBRARY_NAME")"
    encoded_path="$(urlencode "/media/movies")"
    local params=("name=$encoded_name" "collectionType=movies" "paths=$encoded_path" "refreshLibrary=true")
    local query
    query="$(IFS='&'; echo "${params[*]}")"
    curl_json POST "/Library/VirtualFolders?$query" "$ACCESS_TOKEN" \
      '{"LibraryOptions":{"Enabled":true,"EnableRealtimeMonitor":false,"SaveLocalMetadata":false}}' >/dev/null
  fi

  curl_json POST "/Library/Refresh" "$ACCESS_TOKEN" >/dev/null
}

item_visible() {
  local items="$1"
  python3 -c '
import json
import sys

data = json.load(sys.stdin)
items = data.get("Items", [])
names = {item.get("Name", "") for item in items}
paths = {item.get("Path", "") for item in items}
video_extensions = (".mp4", ".m4v", ".mkv", ".mov", ".avi")
ok = any("Syncplay Test Movie" in name for name in names) or any(path.lower().endswith(video_extensions) for path in paths)
raise SystemExit(0 if ok else 1)
' <<< "$items"
}

wait_for_seeded_item() {
  local items
  local query="/Items?userId=$USER_ID&Recursive=true&IncludeItemTypes=Movie&Fields=Path&Limit=50"
  local attempts=$(( (SCAN_TIMEOUT_SECONDS + 1) / 2 ))

  echo "Waiting for Jellyfin to scan the seeded movie"
  for _ in $(seq 1 "$attempts"); do
    items="$(curl_json GET "$query" "$ACCESS_TOKEN")"
    validate_json "$items" "seeded media items"
    if item_visible "$items"; then
      echo "Seeded movie is visible through the Jellyfin API"
      return
    fi
    sleep 2
  done

  echo "Timed out waiting for the seeded movie to appear" >&2
  exit 1
}

print_connection_info() {
  cat <<INFO

Jellyfin test server is ready.
  Local URL:      $SERVER_URL
  Username:       $USERNAME
  Password:       $PASSWORD
  Emulator URL:   http://10.0.2.2:${JELLYFIN_PORT:-8096}
  Phone/TV URL:   http://<host-lan-ip>:${JELLYFIN_PORT:-8096}
  Bind address:   ${JELLYFIN_BIND_ADDRESS:-0.0.0.0}

Use the Phone/TV URL for onn 4K Pro, NVIDIA Shield, and physical Android phones.
INFO
}

command_up() {
  require_tools
  ensure_dirs
  seed_media
  compose up -d
  wait_for_server
  complete_startup
  authenticate
  ensure_library
  wait_for_seeded_item
  print_connection_info
}

command_reset() {
  require_tools
  compose down || true
  rm -rf "$STATE_DIR/config" "$STATE_DIR/cache"
  command_up
}

command_status() {
  require_tools
  compose ps
  echo
  curl_public "/System/Info/Public" || true
  echo
}

case "${1:-help}" in
  up|start)
    command_up
    ;;
  seed)
    require_tools
    seed_media
    ;;
  reset)
    command_reset
    ;;
  status)
    command_status
    ;;
  down|stop)
    require_tools
    compose down
    ;;
  logs)
    require_tools
    shift || true
    if [[ "${1:-}" == "--" ]]; then
      shift
    fi
    compose logs "$@"
    ;;
  help|--help|-h)
    usage
    ;;
  *)
    usage >&2
    exit 1
    ;;
esac
