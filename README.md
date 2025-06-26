# ATAK Cache Manager

A simple Android application for manually managing ATAK (Android Team Awareness Kit) cache files locally on your device.

## Features

- **Offload ATAK Cache**: Moves `statesaver2.sqlite` to a timestamped backup location (manual only)
- **Delete ATAK Cache**: Permanently removes the current `statesaver2.sqlite` file (manual only)
- **Restore ATAK Cache**: Restores the most recent backup to the active location (manual only)
- **Clear ATOS Cache**: Moves `atos_history.sqlite` to a timestamped archive location

> **Note:** The app no longer supports scheduled/automated cache operations. All actions are performed manually by the user.

## ATAK Must Be Closed

Before any ATAK cache operation (offload, delete, restore), the app will always show a warning dialog:

> "ATAK must be closed before performing this operation. Please ensure ATAK is not running, then continue."

You must confirm this warning before proceeding. This is to prevent data corruption or instability in ATAK.

## File Locations

### ATAK Cache Files
- **Active Cache**: `/atak/Databases/statesaver2.sqlite`
- **Backup Location**: `/atak/Databases/backup/statesaver2_[timestamp].sqlite`

### ATOS Cache Files
- **Active Cache**: `/atak/tools/atos/atos_history.sqlite`
- **Archive Location**: `/atak/tools/atos/archive/atos_history_[timestamp].sqlite`

## Requirements

- Android 7.0 (API level 24) or higher
- Storage permissions (READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
- For Android 11+: "All files access" permission

## Installation

1. Download the latest APK from the releases
2. Enable "Install from unknown sources" in your Android settings
3. Install the APK
4. Grant storage permissions when prompted

## Usage

1. Open the ATAK Cache Manager app
2. Grant storage permissions if not already granted
3. Select the desired cache operation:
   - **Offload**: Creates a backup before clearing
   - **Delete**: Permanently removes cache (use with caution)
   - **Restore**: Brings back the most recent backup
   - **Clear ATOS**: Archives ATOS cache with timestamp
4. For ATAK cache operations, you will first see a warning to close ATAK. Confirm to proceed.
5. Confirm the action in the dialog
6. For ATOS or ATAK-running warnings, a Toast notification will show the operation status or warning.

## Permissions

The app requires the following permissions:
- `READ_EXTERNAL_STORAGE`: To read ATAK cache files
- `WRITE_EXTERNAL_STORAGE`: To write backup/archive files
- `MANAGE_EXTERNAL_STORAGE`: For Android 11+ to access all files

## Development

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 36
- Kotlin 1.9+

### Building from Source
```bash
git clone https://github.com/mjthree/atakcache.git
cd atakcache
./gradlew assembleDebug
```

### Project Structure
```
app/src/main/java/com/apexclearcache/app/
├── MainActivity.kt          # Main UI and permission handling
├── CacheManager.kt          # Core cache operations
└── ui/theme/               # Material 3 UI theme
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

## License

This project is open source. Please check the LICENSE file for details.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## Disclaimer

This app is designed for managing ATAK cache files. Always backup important data before performing cache operations. The developers are not responsible for any data loss. **Never perform ATAK cache operations while ATAK is running.**

## Support

For issues and feature requests, please use the GitHub Issues page. 