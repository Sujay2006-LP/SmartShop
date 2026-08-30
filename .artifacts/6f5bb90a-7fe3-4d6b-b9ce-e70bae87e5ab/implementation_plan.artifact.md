# Fix Duplicate Plugin and Missing Version Sync Error

The project is failing to sync because `com.android.application` is requested twice in `app/build.gradle.kts` (once via `alias` and once via `id`). Additionally, the Kotlin Android and Google Services plugins are inconsistently declared, which may lead to further sync issues.

## Proposed Changes

I will consolidate the plugin declarations into the Version Catalog (`libs.versions.toml`) and use aliases consistently across the project.

### Gradle Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Sujay%20l%20patil/AndroidStudioProjects/SmartShop/gradle/libs.versions.toml)
- Add `kotlin-android` plugin with version `2.2.10`.
- Add `google-services` plugin with version `4.4.2`.

#### [MODIFY] [build.gradle.kts (root)](file:///C:/Users/Sujay%20l%20patil/AndroidStudioProjects/SmartShop/build.gradle.kts)
- Use aliases for all plugins.
- Add `kotlin-android` to the root `plugins` block with `apply false`.

#### [MODIFY] [build.gradle.kts (app)](file:///C:/Users/Sujay%20l%20patil/AndroidStudioProjects/SmartShop/app/build.gradle.kts)
- Remove the redundant `id("com.android.application")`.
- Replace `id("org.jetbrains.kotlin.android")` and `id("com.google.gms.google-services")` with their respective aliases.
- Clean up the `plugins` block.

## Verification Plan

### Automated Tests
- Run `./gradlew help` to verify that the project syncs and basic Gradle tasks can run without plugin errors.
