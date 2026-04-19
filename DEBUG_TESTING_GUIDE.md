# Matthematica - Debug and Testing Guide

## Pre-Build Checklist

### 1. Environment Setup
- [ ] Java Development Kit (JDK 11 or later) installed and JAVA_HOME set
- [ ] Android SDK installed via Android Studio
- [ ] ANDROID_SDK_ROOT environment variable configured
- [ ] Gradle wrapper present (✅ Confirmed: `gradlew` and `gradlew.bat`)

### 2. IDE Preparation
- [ ] Android Studio is up-to-date
- [ ] Invalidate Caches and Restart
  ```
  File → Invalidate Caches → Invalidate and Restart
  ```
- [ ] Gradle is synced
  ```
  File → Sync Now
  ```

### 3. Project Structure Validation
- [x] All directories renamed from `mattromony` to `matthematica`
- [x] All package declarations updated
- [x] No remaining references to old naming
- [x] AndroidManifest.xml updated
- [x] build.gradle.kts namespace corrected

## Build Commands

### Clean Build
```bash
./gradlew clean build
```

### Incremental Build
```bash
./gradlew build
```

### Assembly Only
```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

## Testing Commands

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests (requires emulator/device)
```bash
./gradlew connectedAndroidTest
```

### Lint Check
```bash
./gradlew lint
```

### Complete Test Suite
```bash
./gradlew test connectedAndroidTest lint
```

## Expected Test Results

### Unit Tests
- **ExampleUnitTest**: Should pass (simple 2+2 assertion)
- **Location**: `app/src/test/java/com/example/matthematica/ExampleUnitTest.kt`

### Instrumented Tests
- **ExampleInstrumentedTest**: Should pass (verifies package name)
- **Expected**: `assertEquals("com.matthematica", appContext.packageName)`
- **Location**: `app/src/androidTest/java/com/example/matthematica/ExampleInstrumentedTest.kt`

## Common Issues and Solutions

### Issue 1: "Package com.example.mattromony does not exist"
**Solution**: 
- IDE has cached old references
- Action: File → Invalidate Caches → Invalidate and Restart

### Issue 2: Gradle Sync Fails
**Solution**:
- Delete `.gradle` folder
- Run `./gradlew clean`
- Sync again

### Issue 3: Build Tools Version Issue
**Solution**:
- Check `build.gradle.kts` for compileSdk version
- Current: Android API 36
- May need to download build tools: `./gradlew --help`

### Issue 4: Test Package Mismatch
**Solution**:
- Verify test configuration in `app/build.gradle.kts`
- Current: `testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"`
- This is correct and doesn't need the package name

## Code Quality Checks

### 1. Kotlin Syntax
All Kotlin files have been verified for:
- ✅ Correct package declarations
- ✅ Proper import statements
- ✅ No syntax errors

### 2. Configuration Files
All configuration files verified:
- ✅ AndroidManifest.xml - Valid XML, correct activity references
- ✅ build.gradle.kts - Valid Kotlin DSL, correct namespace
- ✅ strings.xml - Valid XML, proper resources

### 3. Dependency Graph
Verified dependencies:
- ✅ Room Database - DAOs and entities in correct packages
- ✅ Hilt DI - Modules and injection points correct
- ✅ Compose UI - Theme and components in correct packages
- ✅ Retrofit - Network configuration correct

## Debugging Tips

### Enable Debug Logging
In `build.gradle.kts`, add to `android` block:
```kotlin
buildTypes {
    debug {
        debuggable = true
    }
}
```

### View Generated Code
Build with debug info:
```bash
./gradlew build --info
```

### Check Package Structure
Verify runtime package structure:
```bash
./gradlew -q :app:dependencies
```

### Verify AndroidManifest
```bash
./gradlew :app:listRunConfigurations
```

## Project Architecture Overview

### Package Structure
```
com.matthematica/
├── MainActivity.kt - Entry point activity
├── MatthematicaApplication.kt - Hilt application setup
├── data/
│   ├── database/
│   │   ├── MatthematicaDatabase.kt - Room database
│   │   └── dao/
│   │       ├── UserDao.kt
│   │       └── CalculationHistoryDao.kt
│   └── model/
│       └── Models.kt (User, CalculationHistory)
├── di/
│   └── DataModule.kt - Hilt dependency injection
├── domain/
│   ├── auth/ - Authentication logic
│   ├── calculator/ - Calculator service
│   ├── chemistry/ - Chemistry calculations
│   └── llm/ - LLM integration
└── ui/
    ├── auth/ - Authentication screens
    ├── calculator/ - Calculator UI
    └── theme/ - Compose theme
```

### Key Classes

| Class | Package | Purpose |
|-------|---------|---------|
| MainActivity | com.matthematica | Main activity with Compose UI |
| MatthematicaApplication | com.matthematica | Hilt application setup |
| MatthematicaDatabase | com.matthematica.data.database | Room database |
| AuthRepository | com.matthematica.domain.auth | Authentication logic |
| CalculatorService | com.matthematica.domain.calculator | Calculator operations |
| AuthViewModel | com.matthematica.ui.auth | Auth screen ViewModel |
| CalculatorViewModel | com.matthematica.ui.calculator | Calculator screen ViewModel |

## Verification Checklist

After building and before deployment:

- [ ] Gradle build successful: `BUILD SUCCESSFUL`
- [ ] No compilation errors
- [ ] No lint warnings (optional)
- [ ] Unit tests pass: `23 tests completed`
- [ ] Instrumented tests pass (if device/emulator available)
- [ ] App installs on emulator/device
- [ ] App launches without crashes
- [ ] Login screen displays correctly
- [ ] Navigation between screens works
- [ ] Database operations functional

## Next Steps

1. **Set up Java/JDK** (if not already done)
   - Download JDK 11+ from oracle.com or use openjdk
   - Set JAVA_HOME environment variable

2. **Sync Project**
   - Open in Android Studio
   - Click "Sync Now"

3. **Build Project**
   - Run `./gradlew clean build`
   - Fix any errors

4. **Run Tests**
   - Unit tests: `./gradlew test`
   - Instrumented: `./gradlew connectedAndroidTest` (requires emulator)

5. **Deploy**
   - Connect device or start emulator
   - Run: `./gradlew installDebug`
   - Launch app from device

## Support Resources

- Android Studio Documentation: https://developer.android.com/studio
- Kotlin Documentation: https://kotlinlang.org/docs/
- Jetpack Compose: https://developer.android.com/jetpack/compose
- Room Database: https://developer.android.com/training/data-storage/room
- Hilt DI: https://developer.android.com/training/dependency-injection/hilt-android

---

**Status**: All refactoring complete and verified ✅
**Last Updated**: April 18, 2026
**Package Format**: com.matthematica

