# Drawing Final Boss

A personal Android project for practising figure drawing with reference photos
and timed sessions. Inspired by Marc Brunet's drawing videos, it combines my
interests in art, software development, and learning through AI-assisted coding.

This is a hobby project under active development.

## Current features

- Three drawing levels: Foundations, Action, and Final Boss.
- Reference photos loaded from Unsplash.
- Preset session durations and a custom duration option.
- A countdown timer with pause, resume, and restart controls.
- A next-pose button to move through the selected reference collection.

## Technology

Kotlin, Jetpack Compose, Material 3, and Coil, built with Gradle.

## Run locally

1. Clone this repository and open its root folder in Android Studio.
2. Install Android SDK 37 and use an Android Studio version compatible with the
   Android Gradle Plugin version in `gradle/libs.versions.toml`.
3. Select an appropriate Gradle JDK in Android Studio. The current
   `gradle.properties` pins the JDK bundled with Android Studio on macOS; update
   or remove those machine-specific JDK settings when using another environment.
4. Let Gradle sync, then run the `app` configuration on an emulator or Android
   device running Android 8.0 (API 26) or newer.

An internet connection is required to load the reference photos. Android Studio
creates `local.properties` for your local SDK path; this file is intentionally
excluded from Git.
