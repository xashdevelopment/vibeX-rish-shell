# VibeX Rish Shell

A terminal/shell application for Android that provides command-line interface capabilities using the Shizuku framework.

## Overview

VibeX Rish Shell is an Android terminal application that enables shell command execution with elevated privileges through Shizuku. It features:

- Terminal-like UI for command input and output
- Shell execution via Shizuku's rikka.shizuku.shell.ShizukuShellLoader
- Tasker plugin integration for automation
- ADRT (Android Debug & Remote Tool) integration for debugging
- Native library support (libQASM.so)

## Project Structure

```
vibeX-project/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/vibe/termplugin/
│   │       │   ├── RishShellActivity.java      # Main terminal activity
│   │       │   ├── NativeLib.java              # Native library wrapper
│   │       │   ├── ShizukuIntegration.java     # Shizuku integration docs
│   │       │   ├── adrt/
│   │       │   │   ├── ADRTSender.java         # Debug event sender
│   │       │   │   └── ADRTLogCatReader.java   # Logcat reader
│   │       │   └── tasker/
│   │       │       └── TaskerActivity.java     # Tasker integration
│   │       ├── res/
│   │       │   ├── drawable/ic_launcher_foreground.xml
│   │       │   ├── mipmap-*/
│   │       │   ├── raw/                        # rish_shizuku_dex (placeholder)
│   │       │   ├── values/
│   │       │   ├── values-v21/
│   │       │   └── xml/tasker_plugin.xml
│   │       ├── shell/
│   │       │   └── rish                        # Shell launcher script
│   │       └── jniLibs/                       # Native libraries
│   │           ├── arm64-v8a/
│   │           └── armeabi-v7a/
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Requirements

To build this project, you need:

- Android Studio Arctic Fox or newer
- Gradle 8.x
- Android SDK 21+ (minSdk 21, targetSdk 34)
- Java 17

## Setup Instructions

### 1. Extract Required Binaries

This project requires two binary files that cannot be included in the source code:

#### rish_shizuku_dex (~59KB)
1. Extract from the original APK: `res/raw/rish_shizuku_dex`
2. Copy to: `app/src/main/res/raw/rish_shizuku_dex`

Or compile from [Shizuku source](https://github.com/RikkaApps/Shizuku):
```bash
./gradlew :shell:assembleRelease
# Find the DEX in shell/build/outputs/dex/
```

#### libQASM.so (~10KB for arm64-v8a, ~9KB for armeabi-v7a)
1. Extract from the original APK:
   - `lib/arm64-v8a/libQASM.so`
   - `lib/armeabi-v7a/libQASM.so`
2. Copy to:
   - `app/src/main/jniLibs/arm64-v8a/libQASM.so`
   - `app/src/main/jniLibs/armeabi-v7a/libQASM.so`

### 2. Build the Project

```bash
# Clone the project
git clone https://github.com/yourusername/vibeX-project.git
cd vibeX-project

# Build
./gradlew assembleDebug
```

## Features

### Shell Execution

The app uses `app_process` to launch the Shizuku shell loader with proper classpath configuration:

```bash
/system/bin/app_process -Djava.class.path="$DEX" /system/bin \
    --nice-name=rish rikka.shizuku.shell.ShizukuShellLoader "$@"
```

### Tasker Integration

The app registers a Tasker plugin activity for automation:
- Action: `com.twohlix.tasker.ACTION_RUN_TASK`
- Parameter: `command` - The shell command to execute

### ADRT Debugging

ADRT (Android Debug & Remote Tool) integration provides:
- Logcat streaming to external debugger
- Breakpoint handling with variable inspection
- Field information broadcasting

## Permissions

- `READ_EXTERNAL_STORAGE` - Required for accessing shared storage (if needed)

## Building

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

## License

This project is based on the decompiled VibeX Rish Shell APK. All components are property of their respective owners.

## Credits

- [Shizuku](https://github.com/RikkaApps/Shizuku) - For the shell execution framework
- [BetterAndroid](https://github.com/BetterAndroid) - For the project template
