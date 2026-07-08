[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)

# ComposeNativeSDL3

A Kotlin/Native port of **Compose Multiplatform** running on **SDL3** for
windowing and input — **no JVM**. Compiles to native binaries for macOS
(arm64), Linux (x64/arm64), Windows (mingwX64).

Rendering is pluggable behind one `RenderBackend`:

- **Skia** (via Skiko klibs) on macOS + Linux — Metal / OpenGL / CPU raster.
- **SDL3** (`SDL3_ttf` + `SDL_RenderGeometry`) on Windows, and on macOS/Linux
  when `-Prenderer=sdl3` is passed.

The Compose **runtime** is the official
`org.jetbrains.compose.runtime:runtime` klibs from Maven — this project
re-implements the layers on top of it (`androidx.compose.ui.*`,
`.foundation.*`, `.animation.*`, `.material3.*`) by **vendoring
upstream Compose Multiplatform verbatim** whenever possible and hand-rolling
project actuals + SDL3 / Skia glue only where needed.

## Module layout

```
compose/
├── ui/               → :ui        (androidx.compose.ui.* + renderers + cinterops)
├── animation-core/   → :animation-core
├── foundation/       → :foundation (foundation.* + non-core animation.*)
├── material3/        → :material3
├── material-symbols/ → :material-symbols (codepoints + Outlined/Rounded/Sharp)
└── native/window/    → :window    (nativeComposeWindow + SDL3 main loop)
```

## demo — widget & feature showcase

<img src="screenshots/demo.png" width="100%" alt="ComposeNativeSDL3 demo" />

`:demo` is a full tour of the re-implemented Compose + Material 3 surface —
30+ sidebar screens covering buttons, text fields, layout, modifiers, shapes,
images, state & recomposition, scrolling & lazy lists, dialogs, icons, canvas,
graphics layers, custom layout, animation and gestures.

```bash
./gradlew :demo:runDebugExecutableMacosArm64      # macOS  (Skia / Metal)
./gradlew :demo:runDebugExecutableLinuxX64        # Linux  (Skia / OpenGL)
gradlew.bat :demo:runDebugExecutableMingwX64      # Windows (SDL3)
```

CLI: `--gpu=…`, `--screen=<Name>` (one screen, no sidebar),
`--screenshot=out.bmp --frames=N`, `--width=W --height=H`.

## apidemo — API Manager

<img src="screenshots/apidemo.png" width="100%" alt="ComposeNativeSDL3 API Manager" />

`:apidemo` is a Postman-style REST client built entirely on the library:
request collections (**packs**, nested sub-packs, linked copies), a
**Session → Pack → Request** inheritance ladder for variables / headers /
query params / client certs, syntax-highlighted JSON / XML / YAML / HTML body
editors, a response viewer with timing, size and TLS-chain inspection, mTLS
client certificates, drag-and-drop tree management, request history and
file-based sessions.

```bash
./gradlew :apidemo:runDebugExecutableMacosArm64
./gradlew :apidemo:runDebugExecutableLinuxX64
gradlew.bat :apidemo:runDebugExecutableMingwX64
```

Add `-Prenderer=sdl3` on macOS/Linux to drop Skiko and use the pure-SDL3
renderer everywhere.

## Minimal app

```kotlin
import androidx.compose.material3.Text
import com.compose.sdl.nativeComposeWindow

fun main() = nativeComposeWindow(title = "Hello") {
    Text("Hello from ComposeNativeSDL3")
}
```

The lambda runs with a `ComposeWindowScope` receiver exposing
`window: ComposeNativeWindow` (`setTitle` / `setSize` / `minimize` /
`maximize` / `setFullscreen` / `close` / …); the same handle is reachable
from any nested composable via `LocalComposeNativeWindow.current`.

Add these to your module's `commonMain.dependencies`:

```kotlin
implementation(project(":window"))            // window + main loop
implementation(project(":material3"))         // Material 3 widgets
implementation(project(":material-symbols"))  // icon-font composables (optional)
```

## Building

- **macOS:** `brew install sdl3` (Skia is the default; Skiko klibs come from
  Maven).
- **Linux:** `sudo apt install libsdl3-dev`.
- **Windows:** SDL3, SDL3_ttf, SDL3_image and FreeType are built from source
  as **static** libraries by `tools/build-all.sh` into the gitignored
  in-repo `libs/`, then linked into the executable — the Windows
  distributable is just `<app>.exe` + `data.kres`, no runtime DLLs.

## Vendoring

The bulk of the `androidx.compose.*` code is vendored byte-for-byte from
`JetBrains/compose-multiplatform-core` and lives under
`<module>/src/vendor/` (gitignored — you re-sync on demand). Each module
carries a `compose-fork.txt` manifest listing which upstream files it pulls
in.

```bash
tools/compose-fork/sync.sh                           # sync every module
tools/compose-fork/sync.sh compose/ui/compose-fork.txt   # one module
```

Upstream ref pinned in `tools/compose-fork/compose-ref.txt`.

See [CLAUDE.md](CLAUDE.md) for the full architecture, source-set hierarchy,
vendoring rules, density flow (physical-pixel Option B), and per-area file
map.

## License

[MIT](LICENSE.md).
