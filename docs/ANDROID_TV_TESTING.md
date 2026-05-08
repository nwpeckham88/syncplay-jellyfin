# Android TV Testing

This guide covers Jellyfin validation on set-top boxes and phones. The priority hardware targets are onn 4K Pro, NVIDIA Shield, and Android phones.

## Local Server

Start a fresh Jellyfin instance before testing:

```bash
scripts/jellyfin-test-env.sh reset
```

Use this server only on a trusted LAN. It uses plain HTTP and known local test credentials.

Use the LAN URL printed by the script on physical devices:

```text
http://<host-lan-ip>:8096
```

Do not enter `localhost` on a TV box or phone.

## ADB Setup

List connected devices:

```bash
scripts/jellyfin-device-test.sh list
```

Run the install/launch/log flow:

```bash
JELLYFIN_BASE_URL=http://<host-lan-ip>:8096 scripts/jellyfin-device-test.sh run
```

Target one device:

```bash
DEVICE_SERIAL=<adb-serial> JELLYFIN_BASE_URL=http://<host-lan-ip>:8096 scripts/jellyfin-device-test.sh run
```

## onn 4K Pro Notes

1. Enable Developer Options and ADB debugging in Android TV settings.
2. Use the same Wi-Fi or Ethernet network as the Jellyfin host.
3. Confirm the host is reachable from a browser or network utility if available.
4. Test D-pad focus carefully; low-cost remotes make missing focus states obvious.
5. Check sleep/wake because set-top boxes often suspend network aggressively.

## NVIDIA Shield Notes

1. Enable Developer Options and network or USB debugging.
2. If using network ADB, confirm the Shield IP has not changed.
3. Test both remote control and game controller input if available.
4. Verify playback start, pause/resume, and back navigation.
5. Shield hardware may direct-play more codecs than lower-end boxes, so use the default H.264/AAC file as the baseline before codec stress tests.

## Phone Notes

1. Use the LAN URL, not `localhost`.
2. Test soft-keyboard behavior on the login screen.
3. Test Wi-Fi reconnect and app foreground/background transitions.
4. If using an emulator, use `http://10.0.2.2:8096`.
5. Genymotion and non-AVD emulators may need `http://<host-lan-ip>:8096` instead.

## Baseline Test Matrix

| Area | Expected result |
| --- | --- |
| Login | `syncplaytest` / `testpass123` succeeds |
| Libraries | Movies library appears |
| Items | Seeded movie appears |
| Thumbnails | Loading thumbnails does not block navigation |
| Playback | H.264/AAC seeded movie starts |
| Remote input | D-pad focus is visible and predictable |
| Back | Back returns to the previous screen |
| Sleep/wake | Connection recovers or shows a clear error |
| Relaunch | Saved config restores or prompts cleanly |

## Codec Stress Tests

Run these only after the baseline passes:

1. HEVC video.
2. AC3 or EAC3 audio.
3. External and embedded subtitles.
4. Higher bitrate files.
5. Long files with seek/resume.

Keep stress media outside git and pass it through `JELLYFIN_TEST_MEDIA` or add it manually under `.jellyfin-test/media`.

## Troubleshooting

### ADB Device Unauthorized

1. Replug USB or re-enable network debugging.
2. Accept the authorization dialog on the device.
3. Run `adb kill-server && adb start-server`.

### Server Timeout

1. Confirm `scripts/jellyfin-test-env.sh status` succeeds on the host.
2. Confirm the host firewall allows port `8096`.
3. Confirm VPN or guest Wi-Fi isolation is not blocking LAN traffic.

### No Visible Focus

1. Capture the exact screen and control.
2. Note the device model and Android version.
3. Capture logcat with `scripts/jellyfin-device-test.sh collect`.

### Playback Starts on Phone but Not TV

1. Re-test with the default seeded H.264/AAC file.
2. Record player engine, codec, and device ABI.
3. Capture Jellyfin logs and device logs.
