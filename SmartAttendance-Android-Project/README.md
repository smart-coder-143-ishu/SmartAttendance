# Smart Attendance - Native Android App

A complete, production-grade native Android Attendance Management application built using **Kotlin**, **Jetpack Compose (Material 3)**, **Room Database (SQLite)**, and the **MVVM architecture**.

---

## 🚀 How to Run in Android Studio

1. **Prerequisites**:
   - Android Studio Hedgehog (2023.1.1) or newer / Iguana / Jellyfish / Ladybug
   - JDK 17 (bundled with Android Studio)
   - Android SDK API 34

2. **Open Project**:
   - Extract the downloaded project ZIP
   - In Android Studio, select **File > Open...** and select the extracted folder.
   - Wait for Gradle to download dependencies and index the project (~1-2 minutes).

3. **Run on Device or Emulator**:
   - Create an Android Virtual Device (AVD) running Android 10+ (API 29 to 34).
   - Or plug in your physical Android phone with **USB Debugging** enabled in Developer Options.
   - Click the green **Run (▶)** button or press `Shift + F10`.

---

## 📦 How to Export and Build the Installable APK

### Method 1: Using Android Studio GUI (Easiest)
1. In Android Studio, go to the top menu: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2. Android Studio will execute `gradlew assembleDebug`.
3. When finished, a notification popup will appear in the bottom-right corner:
   `APK(s) generated successfully: locate`.
4. Click **locate** to open the folder:
   `app/build/outputs/apk/debug/app-debug.apk`
5. Transfer this `.apk` to your Android phone via USB, Google Drive, or WhatsApp, and tap to install!

### Method 2: Generate Signed Release APK (For Production / Distribution)
1. Go to **Build > Generate Signed Bundle / APK...**
2. Choose **APK** and click **Next**.
3. Select an existing keystore or click **Create new...** to create a signing keystore.
4. Choose build variant **release**.
5. Check **V1 (Jar Signature)** and **V2 (Full APK Signature)**.
6. Click **Finish**. The signed APK will be in `app/release/app-release.apk`.

### Method 3: Command Line (Terminal / Mac / Linux / Windows)
```bash
# For Debug APK:
./gradlew assembleDebug

# Output APK path:
# app/build/outputs/apk/debug/app-debug.apk

# For Release APK:
./gradlew assembleRelease

# Install directly to plugged-in Android device via ADB:
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 🏛️ Architecture Overview

- **Room Database with SQLite**:
  - `Student` Entity: Stores name, roll number, class. Unique constraint on `(rollNumber, className)`.
  - `AttendanceRecord` Entity: Stores date and present/absent state. Unique composite index on `(studentId, date)` prevents duplicate entries for the same student on the same date.
  - Foreign key cascade: Deleting a student automatically removes all their attendance records.
- **Repository Pattern**:
  - `AttendanceRepository` abstracts Room DAO operations, manages Coroutines on `Dispatchers.IO`, and provides reactive `Flow` data streams.
- **MVVM Architecture**:
  - ViewModels expose `StateFlow<UiState>` consumed safely by Jetpack Compose screens using `collectAsStateWithLifecycle()`.
- **100% Offline**:
  - Operates locally without requiring internet permissions or remote servers.
