# Manual Testing Guide for Jellyfin Integration

Use this guide when validating the Jellyfin integration on a real Android phone, onn 4K Pro, NVIDIA Shield, or Android emulator.

## Start the Local Server

From the repository root:

```bash
scripts/jellyfin-test-env.sh reset
```

This creates a fresh Jellyfin instance, seeds a Movies library, and prints the URLs to use from host, emulator, and physical devices.

Run this only on a trusted local network. The local harness uses plain HTTP and default test credentials.

Default test account:

```text
Username: syncplaytest
Password: testpass123
```

Use a custom MP4:

```bash
JELLYFIN_TEST_MEDIA=/absolute/path/to/movie.mp4 scripts/jellyfin-test-env.sh reset
```

## Build and Install

```bash
./gradlew assembleDebug
scripts/jellyfin-device-test.sh list
JELLYFIN_BASE_URL=http://<host-lan-ip>:8096 scripts/jellyfin-device-test.sh run
```

The debug app id is `com.reddnek.syncplay.new`.

Launch the Jellyfin Test Runner manually if needed:

```bash
adb shell am start -n com.reddnek.syncplay.new/com.yuroyami.syncplay.JellyfinTestRunner
```

For one target device:

```bash
adb -s <adb-serial> shell am start -n com.reddnek.syncplay.new/com.yuroyami.syncplay.JellyfinTestRunner
```

## Server URL Rules

| Test target | URL to enter |
| --- | --- |
| Android emulator (AVD) | `http://10.0.2.2:8096` |
| Genymotion or other emulator | `http://<host-lan-ip>:8096` |
| Android phone | `http://<host-lan-ip>:8096` |
| onn 4K Pro | `http://<host-lan-ip>:8096` |
| NVIDIA Shield | `http://<host-lan-ip>:8096` |

`localhost` and `127.0.0.1` only work from the development machine itself. They do not work from physical Android devices.

## Debug Test Runner

Debug builds include a launcher activity named "Jellyfin Test Runner".

Use it to run:

1. Authentication test.
2. Library access test.
3. Media browsing test.
4. Stream URL generation test.
5. Full test suite.

The current runner may still require manual entry of server URL and credentials until app-side configuration intake is updated. The device helper passes the server URL extra by default. It only passes username/password extras when `JELLYFIN_PASS_CREDENTIAL_EXTRAS=true` because Android may log intent extras.

## Happy Path Checklist

1. Start the local Jellyfin server with `scripts/jellyfin-test-env.sh reset`.
2. Confirm the server opens from the host at `http://127.0.0.1:8096`.
3. Confirm the server opens from another LAN device at `http://<host-lan-ip>:8096`.
4. Build the debug APK with `./gradlew assembleDebug`.
5. Install and launch the test runner.
6. Log in with `syncplaytest` / `testpass123`.
7. Confirm the Movies library appears.
8. Open the seeded movie item.
9. Start playback through the Syncplay flow.
10. Pause, resume, back out, and relaunch the app.

## Android TV Checklist

Run these checks on onn 4K Pro and NVIDIA Shield when hardware is available:

1. D-pad can move through every login field and action.
2. Focus is visible on all selectable controls.
3. Keyboard entry works with remote and paired keyboard.
4. Back button returns to the previous screen without closing unexpectedly.
5. Playback starts from the seeded H.264/AAC file.
6. Pause/resume works from the remote.
7. Sleep/wake keeps or recovers the Jellyfin connection.
8. Reopening the app preserves or cleanly restores the last server config.
9. Device logs do not contain full access tokens or token-bearing URLs.

## Phone Checklist

1. Test portrait and landscape if the device allows rotation.
2. Verify soft keyboard does not hide login actions.
3. Verify Wi-Fi reconnect behavior.
4. Verify background/foreground behavior during playback setup.
5. Verify the same seeded movie starts playback.

## Logs and Results

Collect Android logs after a run:

```bash
JELLYFIN_BASE_URL=http://<host-lan-ip>:8096 scripts/jellyfin-device-test.sh collect
```

Collect Jellyfin logs:

```bash
scripts/jellyfin-test-env.sh logs -- --tail=300
```

Device logs are written under `test-results/jellyfin/`, which is ignored by git.

## Common Issues

### Device Cannot Reach Server

1. Use the host LAN IP, not `localhost`.
2. Confirm the phone or TV box is on the same network as the host.
3. Disable VPN temporarily.
4. Allow inbound traffic to port `8096` through the host firewall.
5. If you are not testing physical devices, restart with `JELLYFIN_BIND_ADDRESS=127.0.0.1` to avoid LAN exposure.

### Test Runner Does Not Open

1. Rebuild and reinstall with `./gradlew assembleDebug`.
2. Confirm the package is installed: `adb shell pm list packages | grep reddnek.syncplay`.
3. Launch with `com.reddnek.syncplay.new/com.yuroyami.syncplay.JellyfinTestRunner`.

### Seeded Movie Missing

1. Run `scripts/jellyfin-test-env.sh reset`.
2. Check `.jellyfin-test/media/movies` for the MP4.
3. Check Jellyfin logs for scan failures.

### Playback Fails

1. Start with the default H.264/AAC MP4.
2. Collect device logs.
3. Note the player engine in use, device model, Android version, and media details.
