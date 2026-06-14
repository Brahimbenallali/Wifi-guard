# WiFi Guard

Android project prepared to build a debug APK with GitHub Actions.

You do not need Android Studio, Git, Gradle, or admin rights on your PC. The APK is built on GitHub servers.

## Upload to GitHub

1. Create a new repository on GitHub.
2. Open the repository in your browser.
3. Click **Add file** > **Upload files**.
4. Upload all files from this ZIP.
5. Click **Commit changes**.

## Build the APK

1. Open the repository on GitHub.
2. Go to **Actions**.
3. Select **Build Android Debug APK**.
4. Click **Run workflow**.
5. Wait for the build to finish.

## Download the APK

1. Open the completed workflow run.
2. Scroll down to **Artifacts**.
3. Download **wifi-guard-debug-apk**.
4. Extract the downloaded ZIP.
5. Install the `.apk` file on your Android phone.

## Included project files

- `app/`
- `settings.gradle.kts`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `gradle.properties`
- `.github/workflows/android-debug-apk.yml`

`local.properties` is not required on GitHub because the workflow installs and configures the Android SDK automatically.
