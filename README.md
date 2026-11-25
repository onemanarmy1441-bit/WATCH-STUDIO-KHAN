# WATCH STUDIO – Galaxy Watch Face Transfer Prototype

Android native Kotlin + Jetpack Compose app. Package: `com.alikhan.watch`.

## Included
- Premium face gallery (six built-in static designs)
- 240x240 RGB565 image generation
- BLE scan and device connection
- Looks for service `0x18D0` and write characteristic `0x2D01`
- Sends the observed setup bytes `AB 06 2B 1C 20 9D`
- Sends 7,200 packets in the observed format `AB 2C + 16-bit big-endian index + 16 image bytes`

## Important protocol limitation
This is an experimental sender based on one captured transfer. The setup command's meaning, device-side completion response, and whether the watch accepts arbitrary images are not fully verified. The app reports when all packets have been sent; this is NOT proof that the watch applied the face. The app does not install animated watch faces or live complications. The captured protocol only supports a static 240x240 image payload so far.

The target watch should expose the observed BLE service/characteristic. If the watch requires additional initialization, pairing, notification setup, pacing, checksum, or a different image header, transfer may fail. Test with a non-critical custom face.

## Build APK with GitHub Actions
1. Upload the contents of this folder to the root of a GitHub repository.
2. Open **Actions**.
3. Run **Build Android APK**.
4. Download artifact `ali-khan-watch-debug-apk`.
5. Extract and install `app-debug.apk` on your Android phone.
6. Grant Nearby devices/Bluetooth permissions when prompted.

Minimum Android: 8.0 (API 26). Android 12+ requires Nearby devices permission.
