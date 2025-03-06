# Build Guide for Syncplay with Jellyfin Integration

## Prerequisites
1. Android Studio or VS Code with Android extensions
2. Android SDK (minimum SDK 21)
3. Java Development Kit 17 or later
4. Git

## Setup Development Environment

### Using VS Code
1. Install Extensions:
   - Android (from Microsoft)
   - Kotlin Language
   - Gradle Language

2. Clone Repository:
```bash
git clone <repository-url>
cd syncplay-mobile
```

3. Open Project:
   - Open VS Code in project directory
   - VS Code should automatically detect Gradle project

### Build Configuration

1. Debug Build (recommended for testing):
```bash
# Using VS Code tasks
Ctrl/Cmd + Shift + P -> "Tasks: Run Task" -> "Build & Install"

# Or using terminal
./gradlew assembleDebug
```

2. Release Build:
```bash
./gradlew assembleRelease
```

## Running the App

### Method 1: VS Code Launch Configuration
1. Open Command Palette (Ctrl/Cmd + Shift + P)
2. Select "Debug: Start Debugging"
3. Choose "Debug App" configuration

### Method 2: Command Line
```bash
# Install debug build
./gradlew installDebug

# Launch main app
adb shell am start -n com.yuroyami.syncplay/.HomeActivity

# Launch test runner (debug builds only)
adb shell am start -n com.yuroyami.syncplay/.JellyfinTestRunner
```

## Testing the Integration

### Running Tests
1. Unit Tests:
   ```bash
   # VS Code Task
   Tasks: Run Task -> Run Unit Tests

   # Terminal
   ./gradlew testDebugUnitTest
   ```

2. Integration Tests:
   ```bash
   # VS Code Task
   Tasks: Run Task -> Run Integration Tests

   # Terminal
   ./gradlew connectedDebugAndroidTest
   ```

3. Full Test Suite:
   ```bash
   # VS Code Task
   Tasks: Run Task -> Full Test Suite
   ```

### Manual Testing
1. Build and install debug variant
2. Launch "Jellyfin Test Runner" from app launcher
3. Enter Jellyfin server details
4. Run desired tests

## Troubleshooting

### Common Build Issues

1. Gradle Sync Failed
   ```bash
   # Clean project
   ./gradlew clean
   
   # Delete Gradle cache
   rm -rf ~/.gradle/caches/
   
   # Sync again
   ./gradlew build
   ```

2. SDK Location Not Found
   - Set ANDROID_SDK_ROOT environment variable
   - Or create local.properties with sdk.dir property

3. Build Tools Missing
   ```bash
   # Using sdkmanager
   sdkmanager "build-tools;33.0.0"
   ```

### Runtime Issues

1. App Crashes on Launch
   - Check logcat output:
   ```bash
   adb logcat | grep -i syncplay
   ```
   
2. Jellyfin Connection Issues
   - Verify server URL format
   - Check network connectivity
   - Confirm server is accessible

3. Performance Issues
   - Enable debug logging
   - Monitor performance metrics
   - Check device resources

## Development Workflow

1. Make Changes:
   - Edit code in VS Code
   - Use hot reload when possible
   - Run tests frequently

2. Local Testing:
   - Use debug build
   - Run test runner
   - Check performance metrics

3. Building for Release:
   ```bash
   # Generate release APK
   ./gradlew assembleRelease
   
   # Location: androidApp/build/outputs/apk/release/
   ```

## IDE Integration

### VS Code Tasks
- Build & Install: Full build and installation
- Run Unit Tests: Execute unit test suite
- Run Integration Tests: Execute Android tests
- Full Test Suite: Complete test execution

### Launch Configurations
- Debug App: Launch main application
- Run Tests: Launch test runner
- Build & Test: Combined build and test

## Getting Help
1. Check error logs:
   ```bash
   # Application logs
   adb logcat -s Syncplay

   # Build logs
   ./gradlew assembleDebug --info
   ```

2. Debug Tools:
   - Use Jellyfin Test Runner
   - Monitor API metrics
   - Check system logs
