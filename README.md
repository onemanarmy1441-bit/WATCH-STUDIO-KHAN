# ALI KHAN WATCH

Android native prototype source project.

## Current prototype
- Premium dark/gold interface
- Home, Studio, Library and Settings screens
- Round analog watch-face preview
- Category selector

## Not implemented yet
- Bluetooth connection and watch-face transfer
- Verified watch-face file format / protocol
- Real sensor data integration
- Animated watch-face export
- Custom dial installation

These features depend on the target watch firmware and its proprietary protocol. The UUIDs observed so far are not enough to safely identify commands or file formats.

## Build APK without Android Studio
1. Create a GitHub repository.
2. Upload the contents of this folder to the repository root.
3. Open the Actions tab.
4. Run `Build Android APK` (or let it run after pushing to main).
5. Open the successful workflow run and download the `ali-khan-watch-debug-apk` artifact.
6. Extract the artifact and install `app-debug.apk` on your Android phone. Allow installation from that source if Android asks.

Package: `com.alikhan.watch`
Minimum Android: 8.0 (API 26)
