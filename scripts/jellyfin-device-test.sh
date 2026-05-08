#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APP_ID="${SYNCPLAY_DEBUG_APP_ID:-com.reddnek.syncplay.new}"
RUNNER_ACTIVITY="${SYNCPLAY_JELLYFIN_RUNNER_ACTIVITY:-com.yuroyami.syncplay.JellyfinTestRunner}"
USERNAME="${JELLYFIN_TEST_USERNAME:-syncplaytest}"
PASSWORD="${JELLYFIN_TEST_PASSWORD:-testpass123}"
RESULT_ROOT="${JELLYFIN_DEVICE_RESULTS_DIR:-$ROOT_DIR/test-results/jellyfin}"
PASS_CREDENTIAL_EXTRAS="${JELLYFIN_PASS_CREDENTIAL_EXTRAS:-false}"

usage() {
  cat <<'USAGE'
Usage: scripts/jellyfin-device-test.sh <command>

Commands:
  list       List connected Android devices with model/API/ABI/form-factor hints
  install    Install the debug APK on connected devices
  launch     Launch Jellyfin Test Runner on connected devices
  collect    Pull a filtered logcat snapshot into test-results/jellyfin
  run        Install, launch, and collect logs
  help       Show this help

Environment:
  JELLYFIN_BASE_URL          Required for launch/run. Use LAN URL for physical devices.
  JELLYFIN_TEST_USERNAME     Default: syncplaytest
  JELLYFIN_TEST_PASSWORD     Default: testpass123
  DEVICE_SERIAL              Optional: target one adb serial instead of all devices
  SYNCPLAY_DEBUG_APK         Optional: explicit debug APK path
  SYNCPLAY_DEBUG_APP_ID      Default: com.reddnek.syncplay.new
  JELLYFIN_PASS_CREDENTIAL_EXTRAS  Pass username/password intent extras, default: false

Examples:
  scripts/jellyfin-device-test.sh list
  JELLYFIN_BASE_URL=http://192.168.1.42:8096 scripts/jellyfin-device-test.sh run
  DEVICE_SERIAL=abc123 JELLYFIN_BASE_URL=http://10.0.2.2:8096 scripts/jellyfin-device-test.sh launch

The current debug runner may still require manual entry until app-side config intake lands.
By default this script only passes the server URL extra; password extras are opt-in because
Android may record intent extras in local device logs.
USAGE
}

require_adb() {
  if ! command -v adb >/dev/null 2>&1; then
    echo "Missing required command: adb" >&2
    exit 1
  fi
}

connected_devices() {
  adb devices | awk 'NR > 1 && $2 == "device" { print $1 }'
}

validate_serial() {
  local serial="$1"
  if [[ ! "$serial" =~ ^[A-Za-z0-9._:-]+$ ]]; then
    echo "Rejecting unexpected adb serial: $serial" >&2
    return 1
  fi
}

target_devices() {
  if [[ -n "${DEVICE_SERIAL:-}" ]]; then
    validate_serial "$DEVICE_SERIAL"
    echo "$DEVICE_SERIAL"
    return
  fi
  while IFS= read -r serial; do
    [[ -z "$serial" ]] && continue
    validate_serial "$serial" && echo "$serial"
  done < <(connected_devices)
}

require_devices() {
  local devices
  devices="$(target_devices)"
  if [[ -z "$devices" ]]; then
    echo "No connected adb devices found. Run 'adb devices -l' and authorize the device." >&2
    exit 1
  fi
}

adb_for() {
  local serial="$1"
  shift
  adb -s "$serial" "$@"
}

device_prop() {
  local serial="$1"
  local prop="$2"
  adb_for "$serial" shell getprop "$prop" | tr -d '\r'
}

is_tv_device() {
  local serial="$1"
  local characteristics
  characteristics="$(device_prop "$serial" ro.build.characteristics)"
  if [[ "$characteristics" == *tv* ]]; then
    return 0
  fi
  adb_for "$serial" shell pm list features 2>/dev/null | tr -d '\r' | grep -q 'android.software.leanback'
}

find_debug_apk() {
  if [[ -n "${SYNCPLAY_DEBUG_APK:-}" ]]; then
    if [[ ! -f "$SYNCPLAY_DEBUG_APK" ]]; then
      echo "SYNCPLAY_DEBUG_APK does not exist: $SYNCPLAY_DEBUG_APK" >&2
      exit 1
    fi
    validate_apk "$SYNCPLAY_DEBUG_APK"
    printf '%s' "$SYNCPLAY_DEBUG_APK"
    return
  fi

  local apk
  apk="$(find "$ROOT_DIR/androidApp/build/outputs/apk" -type f -name '*debug*.apk' 2>/dev/null | grep -E 'universal|withLibs|debug' | sort | head -n 1 || true)"
  if [[ -z "$apk" ]]; then
    echo "No debug APK found. Build one first with ./gradlew assembleDebug." >&2
    exit 1
  fi
  validate_apk "$apk"
  printf '%s' "$apk"
}

validate_apk() {
  local apk="$1"
  if ! command -v file >/dev/null 2>&1; then
    echo "Warning: 'file' command not available; skipping APK validation" >&2
    return
  fi
  if ! file "$apk" | grep -q 'Zip archive data'; then
    echo "Selected APK does not look like a valid Android package: $apk" >&2
    exit 1
  fi
}

command_list() {
  require_adb
  require_devices
  while IFS= read -r serial; do
    [[ -z "$serial" ]] && continue
    local model manufacturer sdk abi form
    model="$(device_prop "$serial" ro.product.model)"
    manufacturer="$(device_prop "$serial" ro.product.manufacturer)"
    sdk="$(device_prop "$serial" ro.build.version.sdk)"
    abi="$(device_prop "$serial" ro.product.cpu.abilist)"
    form="phone/tablet"
    if is_tv_device "$serial"; then
      form="android-tv"
    fi
    printf '%s\t%s %s\tAPI %s\t%s\t%s\n' "$serial" "$manufacturer" "$model" "$sdk" "$form" "$abi"
  done <<< "$(target_devices)"
}

command_install() {
  require_adb
  require_devices
  local apk
  apk="$(find_debug_apk)"
  echo "Installing $apk"
  while IFS= read -r serial; do
    [[ -z "$serial" ]] && continue
    echo "Installing on $serial"
    adb_for "$serial" install -r "$apk"
  done <<< "$(target_devices)"
}

require_base_url() {
  if [[ -z "${JELLYFIN_BASE_URL:-}" ]]; then
    cat >&2 <<'MSG'
JELLYFIN_BASE_URL is required for launch/run.
Use http://<host-lan-ip>:8096 for physical phones, onn 4K Pro, and NVIDIA Shield.
Use http://10.0.2.2:8096 for the Android emulator.
MSG
    exit 1
  fi
}

command_launch() {
  require_adb
  require_devices
  require_base_url
  while IFS= read -r serial; do
    [[ -z "$serial" ]] && continue
    echo "Launching Jellyfin Test Runner on $serial with $JELLYFIN_BASE_URL"
    local args=(
      shell am start
      -n "$APP_ID/$RUNNER_ACTIVITY"
      --es jellyfinBaseUrl "$JELLYFIN_BASE_URL"
    )
    if [[ "$PASS_CREDENTIAL_EXTRAS" == "true" ]]; then
      echo "Warning: passing Jellyfin username/password through adb intent extras for $serial" >&2
      args+=(--es jellyfinUsername "$USERNAME" --es jellyfinPassword "$PASSWORD")
    fi
    adb_for "$serial" "${args[@]}" >/dev/null
  done <<< "$(target_devices)"
}

safe_serial() {
  printf '%s' "$1" | tr -c 'A-Za-z0-9._-' '_'
}

command_collect() {
  require_adb
  require_devices
  local timestamp
  timestamp="$(date -u +%Y%m%dT%H%M%SZ)"
  while IFS= read -r serial; do
    [[ -z "$serial" ]] && continue
    local dir
    dir="$RESULT_ROOT/$timestamp/$(safe_serial "$serial")"
    mkdir -p "$dir"
    {
      echo "serial=$serial"
      echo "model=$(device_prop "$serial" ro.product.model)"
      echo "manufacturer=$(device_prop "$serial" ro.product.manufacturer)"
      echo "sdk=$(device_prop "$serial" ro.build.version.sdk)"
      echo "abi=$(device_prop "$serial" ro.product.cpu.abilist)"
      echo "base_url=${JELLYFIN_BASE_URL:-unset}"
    } > "$dir/device-info.txt"
    adb_for "$serial" logcat -d -v time \
      | grep -iE 'syncplay|jellyfin|ktor|exoplayer|media3|vlc|mpv' \
      | grep -viE 'access[_ -]?token|api[_ -]?key|jellyfinPassword|password|Token=' \
      > "$dir/logcat-filtered.txt" || true
    echo "Collected logs for $serial in $dir"
  done <<< "$(target_devices)"
}

case "${1:-help}" in
  list)
    command_list
    ;;
  install)
    command_install
    ;;
  launch)
    command_launch
    ;;
  collect)
    command_collect
    ;;
  run)
    command_install
    command_launch
    command_collect
    ;;
  help|--help|-h)
    usage
    ;;
  *)
    usage >&2
    exit 1
    ;;
esac
