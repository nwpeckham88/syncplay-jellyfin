# Testing Jellyfin Integration

This project has a local Jellyfin test harness for Android development. It creates a fresh Jellyfin server, seeds one known movie file, completes the first-run wizard through Jellyfin's API, and exposes the server on port `8096` for Android phones and Android TV devices.

Generated server state and downloaded media live under `.jellyfin-test/` and are ignored by git. This harness is for trusted local testing only. It uses known test credentials and plain HTTP so phones and Android TV boxes can connect on a LAN.

## Prerequisites

1. Docker with Compose support. Compose v2 is recommended; the script auto-detects `docker-compose` and `docker compose`, and you can set `COMPOSE_CMD` if your setup needs an explicit command.
2. `curl` and `python3` for the bootstrap script.
3. Android SDK platform tools for device testing.
4. Optional: a local MP4 file to use instead of the default free sample video.

## Fresh Jellyfin Server

Start or recreate the local test server from the repository root:

```bash
scripts/jellyfin-test-env.sh up
```

To force a clean Jellyfin instance, removing generated Jellyfin config and cache:

```bash
scripts/jellyfin-test-env.sh reset
```

The script will:

1. Create `.jellyfin-test/config`, `.jellyfin-test/cache`, and `.jellyfin-test/media`.
2. Copy `JELLYFIN_TEST_MEDIA` into the test library, or download a free H.264/AAC MP4 sample.
3. Start `docker-compose.jellyfin.yml`.
4. Complete Jellyfin startup setup.
5. Create a test Movies library mounted from `/media/movies`.
6. Poll the Jellyfin API until the seeded movie appears.

Default credentials:

```text
Username: syncplaytest
Password: testpass123
```

Use your own media file:

```bash
JELLYFIN_TEST_MEDIA=/absolute/path/to/movie.mp4 scripts/jellyfin-test-env.sh reset
```

Use another Jellyfin image or host port:

```bash
JELLYFIN_IMAGE=jellyfin/jellyfin:10.10.7 JELLYFIN_PORT=8097 scripts/jellyfin-test-env.sh reset
```

Bind only to localhost for host or emulator-only testing:

```bash
JELLYFIN_BIND_ADDRESS=127.0.0.1 scripts/jellyfin-test-env.sh reset
```

Verify a downloaded media file when you know its SHA-256:

```bash
JELLYFIN_TEST_MEDIA_SHA256=<sha256> scripts/jellyfin-test-env.sh reset
```

If your machine uses Compose v2 only:

```bash
COMPOSE_CMD="docker compose" scripts/jellyfin-test-env.sh up
```

## Server URLs

Use the URL that matches where the Android app is running:

| Target | Server URL |
| --- | --- |
| Host browser | `http://127.0.0.1:8096` |
| Android emulator (AVD) | `http://10.0.2.2:8096` |
| Genymotion or other emulator | `http://<host-lan-ip>:8096` |
| Physical phone | `http://<host-lan-ip>:8096` |
| onn 4K Pro / NVIDIA Shield | `http://<host-lan-ip>:8096` |

Do not use `localhost` from a physical Android device or Android TV box. It points at the device itself, not the development machine.

The default Compose binding is `0.0.0.0:8096` so physical devices can connect. Run this only on a trusted LAN or set `JELLYFIN_BIND_ADDRESS=127.0.0.1` when physical device access is not needed.

Find the host LAN IP on Linux:

```bash
hostname -I | awk '{print $1}'
```

## Jellyfin API Contract Checks

The bootstrap script validates the same core contracts the app relies on:

1. `POST /Users/AuthenticateByName` with body fields `Username` and `Pw`.
2. Jellyfin's `Authorization: MediaBrowser ...` client identity header.
3. `GET /UserViews?userId=...` for libraries.
4. `GET /Items?userId=...&Recursive=true` for seeded media.
5. Query-result envelopes with `Items` and `TotalRecordCount`, not raw lists.

For manual API checks after startup:

```bash
scripts/jellyfin-test-env.sh status
```

## Android Device Smoke Test

Build the debug APK first:

```bash
./gradlew assembleDebug
```

List connected devices:

```bash
scripts/jellyfin-device-test.sh list
```

Launch the debug Jellyfin test runner on all connected devices:

```bash
JELLYFIN_BASE_URL=http://<host-lan-ip>:8096 scripts/jellyfin-device-test.sh run
```

Target one device:

```bash
DEVICE_SERIAL=<adb-serial> JELLYFIN_BASE_URL=http://<host-lan-ip>:8096 scripts/jellyfin-device-test.sh run
```

The helper installs the debug APK, launches `com.reddnek.syncplay.new/com.yuroyami.syncplay.JellyfinTestRunner`, and stores filtered logcat snapshots under `test-results/jellyfin/`.

The current debug runner may still require manual entry until app-side config intake is updated. The script passes the server URL extra by default. It only passes username/password intent extras when `JELLYFIN_PASS_CREDENTIAL_EXTRAS=true` because Android may log intent extras.

## Test Scenarios

### Basic Authentication

```text
Server URL: http://<server-ip>:8096
Username: syncplaytest
Password: testpass123
```

Expected result: login succeeds and the Jellyfin browser can load server libraries.

### Media Library Access

1. Verify the Movies library is visible.
2. Verify the seeded movie appears.
3. Verify thumbnails do not block browsing if they are still loading.
4. Verify empty or loading states are explicit, not a permanent spinner.

### Media Playback

1. Select the seeded movie.
2. Verify stream URL generation.
3. Verify playback starts in the Syncplay flow.
4. Verify pause/resume and back navigation.

### Android TV Input

1. Navigate the login form with a D-pad.
2. Confirm visible focus for buttons, fields, libraries, and media items.
3. Use Back, Home, sleep/wake, and app relaunch.
4. Confirm playback can be started and paused using the remote.

### Error Cases

Test these cases once the happy path works:

1. Wrong server URL.
2. Invalid credentials.
3. Server down during login.
4. Server down after library load.
5. Device switches Wi-Fi networks.
6. Test user lacks media permissions.

## Troubleshooting

### Server Not Reachable From Device

1. Use `http://<host-lan-ip>:8096`, not `localhost`.
2. Confirm the host and device are on the same network.
3. Check VPN or firewall rules on the host.
4. Verify the server is reachable from another device browser.

### Missing Seeded Movie

1. Run `scripts/jellyfin-test-env.sh status`.
2. Check `scripts/jellyfin-test-env.sh logs -- --tail=200`.
3. Run `scripts/jellyfin-test-env.sh reset` to recreate config/cache.
4. Confirm `.jellyfin-test/media/movies` contains an MP4 file.

### Authentication Failures

1. Confirm the test credentials are `syncplaytest` / `testpass123` unless overridden.
2. Reset the server if an older generated Jellyfin config used different credentials.
3. Check Jellyfin logs for lockout or startup wizard issues.
4. Do not reuse these default credentials on any production or internet-reachable Jellyfin server.

### Streaming Issues

1. Start with the default H.264/AAC MP4 baseline.
2. Confirm the device can reach Jellyfin directly in a browser or Jellyfin client.
3. Capture logs with `scripts/jellyfin-device-test.sh collect`.
4. Add HEVC, AC3/EAC3, subtitles, and high-bitrate files only after the baseline passes.

## Logs to Collect

1. `scripts/jellyfin-test-env.sh logs -- --tail=300`
2. `scripts/jellyfin-device-test.sh collect`
3. Exact server URL used on the device.
4. Device model, Android API level, and ABI from `scripts/jellyfin-device-test.sh list`.
5. Media file details when using custom media.
