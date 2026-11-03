# Drawing Final Boss

A personal Android project for practising figure drawing with reference photos
and timed sessions. Inspired by Marc Brunet's drawing videos, it combines my
interests in art, software development, and learning through AI-assisted coding.

This is a hobby project under active development.

## Current features

- Three drawing levels: Foundations, Action, and Final Boss.
- A branded opening sequence, custom studio wordmark, adaptive launcher mark,
  and warm sketchbook-inspired interface.
- A curated reference library loaded from Supabase PostgreSQL and Storage.
- Preset session durations and a custom duration option.
- A countdown timer with pause, resume, and restart controls.
- A next-pose button to move through the selected reference collection.
- Published references load per difficulty in pages of 24, with more fetched near
  the end of the current page. Photo IDs remain stable when pages are appended.
- Loading, empty, retry, and photo-error states; metadata fields can be blank.
- The timer starts after the image loads and pauses while the app is in the
  background. It uses elapsed time rather than assuming each tick is exact.
- The first page of metadata is saved per level for network failures. Coil caches
  viewed images separately; an unseen photo still requires internet. Refreshing
  successfully replaces the saved page, including when the library becomes empty.

## Technology

Kotlin, Jetpack Compose, Material 3, lifecycle ViewModel, OkHttp, and Coil, built
with Gradle. Supabase is accessed through its REST API; visitors need no login.

## Run locally

1. Clone this repository and open its root folder in Android Studio.
2. Install Android SDK 37 and use an Android Studio version compatible with the
   Android Gradle Plugin version in `gradle/libs.versions.toml`.
3. Select an appropriate Gradle JDK in Android Studio. The current
   `gradle.properties` pins the JDK bundled with Android Studio on macOS; update
   or remove those machine-specific JDK settings when using another environment.
4. Let Gradle sync, then run the `app` configuration on an emulator or Android
   device running Android 8.0 (API 26) or newer.

An internet connection is required for new reference photos. Android Studio
creates `local.properties` for your local SDK path; this file is intentionally
excluded from Git.

## Supabase configuration

The app is configured for `https://luptgjbydcbcvbgjxkit.supabase.co` using the
project's **publishable** client key. These client settings are public information,
not administrative credentials. You can override either setting in
`local.properties` or with Gradle `-P` properties:

```properties
SUPABASE_URL=https://YOUR_PROJECT.supabase.co
SUPABASE_PUBLISHABLE_KEY=sb_publishable_YOUR_KEY
```

Use the base URL without `/rest/v1/`. Never configure a service-role/secret key.
The client sends the publishable key as `apikey`, not as a bearer JWT.

The existing `public.reference_photos` table supplies:

| Column | Use |
| --- | --- |
| `id` | UUID primary key; stable identity and pagination cursor |
| `storage_path` | Exact object name inside the `references` bucket |
| `difficulty` | `1`, `2`, or `3` |
| `status` | Only `published` rows are requested |
| `title`, `pose_type` | Optional display and accessibility descriptions |
| `is_full_body` | Optional manual label; null means unknown |
| `attribution`, `license` | Optional display information while cataloguing |
| `source_url` | Retained in the database for curation; not required by the client |

Keep Row Level Security enabled with SELECT for `anon` and `authenticated` only
where `status = 'published'`. The public client must have no INSERT, UPDATE, or
DELETE policies. Upload images and manage metadata through the Supabase dashboard.
The `references` bucket must permit public image reads; public does not mean
anonymous uploads. Draft images in a public bucket can still be fetched if their
URLs are known; keep anything private in a private bucket.

## Add or replace a reference

1. Upload a photo you can redistribute into the `references` bucket.
2. Copy its exact object name into `storage_path`, **without** the bucket name.
   Capitalization matters: `Level-1/1.jpg` and `level-1/1.jpg` are different.
   Paths containing spaces are supported and URL-encoded by the app.
3. Set difficulty to `1`, `2`, or `3` regardless of the folder's name. A folder
   called `Boss Level` can therefore use difficulty `3`.
4. Fill the descriptive/source/license information and review the photo manually.
   The app does not infer full-body, gender, or difficulty from image contents.
5. Change that row's status to `published` when ready. Reopen the app or tap
   **Refresh references** on the home screen to fetch it without a new app build.

An empty level means there are no rows visible to the client for that difficulty.
Draft rows intentionally do not appear. Run `supabase/verify_catalog.sql` in the
Supabase SQL Editor to diagnose statuses, RLS policies, and paths that don't match
uploaded images. `supabase/catalog_index.sql` adds an optional pagination index.
Neither script publishes photos or changes Storage objects.

For public traffic, resize photos before upload (for example JPEG/WebP up to
1600 pixels on the long side), use unique/versioned names for replacements, and
monitor Storage bandwidth and database limits. This implementation provides a
shared Android library, not unlimited free hosting. iOS requires a separate app.

## Verify and install

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

The debug APK is at `app/build/outputs/apk/debug/app-debug.apk`. You can install it
on Android 8.0+ for testing. For distribution, generate a signed release bundle
using your own signing key and use Google Play testing/production tracks. Never
commit a signing key or its passwords. A public release also needs device testing
and a review of photo permissions and credits.
