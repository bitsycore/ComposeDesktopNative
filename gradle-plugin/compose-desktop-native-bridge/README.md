# Compose Desktop Native Bridge

`com.bitsycore.compose-desktop-native.bridge` lets an app declare the **official Compose
Multiplatform coordinates once in `commonMain`** and run on every platform CMP
supports **plus** the port's Kotlin/Native desktop targets (mingwX64,
linuxX64, linuxArm64, macosArm64 — no JVM).

Gradle dependency-substitution rules cannot ship inside a Maven artifact, so
this plugin carries them instead: on every configuration belonging to a native
desktop target it substitutes the official `org.jetbrains.compose.*`
coordinates for the published `com.bitsycore.compose.sdl:*` klibs. All other
targets (android / jvm / iOS / wasm) keep resolving the official artifacts
untouched. `org.jetbrains.compose.runtime` is never substituted — the official
runtime klibs serve every target.

## Usage

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        // GitHub Packages requires authentication even for public packages.
        maven("https://maven.pkg.github.com/bitsycore/compose-desktop-native") {
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.token").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.bitsycore.compose-desktop-native.bridge") version "<release-version>"
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven.pkg.github.com/bitsycore/compose-desktop-native") { credentials { /* same */ } }
        google()
        mavenCentral()
    }
}
```

```kotlin
// module build.gradle.kts — official coords, everywhere
kotlin {
    macosArm64(); linuxX64(); mingwX64()   // + jvm()/android()/ios if you like
    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.compose.runtime:runtime:1.11.1")
            implementation("org.jetbrains.compose.ui:ui:<cmp-version>")
            implementation("org.jetbrains.compose.foundation:foundation:<cmp-version>")
            implementation("org.jetbrains.compose.material3:material3:<cmp-m3-version>")
        }
    }
}
```

The plugin can also be applied to a single module's `build.gradle.kts` instead
of settings (it then bridges only that module's configurations).

## Notes

- The substituted klib version defaults to the plugin's own version (both ship
  from the same tag). Override with `composeDesktopNative.version=<x>` in
  `gradle.properties` when mixing releases.
- Match `<cmp-version>` to the CMP release the port's vendored sources track
  (see `scripts/compose-fork/compose.properties` at the release tag) —
  substitution replaces whatever version you request on native, but your
  jvm/android targets resolve the official artifacts at the version you write.
- Requires Gradle 8.8+ when applied in settings (`gradle.lifecycle.beforeProject`).
- App windowing/main-loop (`com.bitsycore.compose.sdl:window`) and the icon
  font module (`material-symbols`) are the port's own APIs — depend on them
  directly; no substitution involved.
