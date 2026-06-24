# CLAUDE.md

Guidance for Claude Code working in this repository. This is the only
context the next agent will have — read it first.

## Project Overview

**ComposeNativeSDL3** — a Kotlin/Native subset of Compose Desktop running
on SDL3. No JVM. Compiles to native binaries for macOS (arm64), Linux
(x64/arm64), and Windows (mingwX64).

The project re-implements just enough of `androidx.compose.*` to host
Composable hierarchies, measure/place them as a layout tree, dispatch
SDL3 input events into the tree, and render them. Rendering is pluggable:

- **Skia** (via Skiko klibs) on macOS + Linux — Metal / OpenGL / CPU raster
- **SDL3** (SDL3_ttf + `SDL_RenderGeometry`) on Windows, and on
  macOS/Linux when `-Prenderer=sdl3` is set

## Module Layout

Local module names drop the `compose-desktop-native-` / `compose-desktop-`
prefix for terseness; publication artifact IDs add it back (so consumers see
`compose-desktop-native-core` etc. on the dependency line). All modules share
the `com.compose.desktop.native` Kotlin package; the re-implemented Compose
APIs keep their upstream `androidx.compose.*` names, in `core/commonMain`.

- `core/` (publishes as `compose-desktop-native-core`) — renderer-agnostic
  base: the `androidx.compose.foundation` / `.ui` / `.animation` re-impl,
  `RenderBackend` interface, `GpuMode`, `SDL3Backend`, window / clipboard /
  event / resource IO, and the bundled default font. Owns the `sdl3`
  cinterop. **No Material code** — Material widgets live in `:material`.
- `material/` (publishes as `compose-desktop-native-material`) — Material
  widgets re-implemented on top of `:core` (Button / Text / MaterialTheme /
  Surface / TextField / Slider / Switch / Checkbox / Radio / Chip / Card /
  Dialog / DropdownMenu / SegmentedButton / Snackbar / Tooltip /
  ProgressIndicator). Apps that only want the foundation+ui base without
  Material can skip pulling this in.
- `renderer-sdl3/` (publishes as `compose-desktop-native-renderer-sdl3`) —
  pure-SDL3 renderer (+ `sdl3_ttf`, `sdl3_image`, `freetype` cinterops).
  Exposes `createRenderBackend(...)` / `rendererPreferredGpuMode()`. All
  four native targets.
- `renderer-skia/` (publishes as `compose-desktop-native-renderer-skia`) —
  Skia/Skiko renderer (Metal / OpenGL / CPU bridges). Same two functions.
  **macOS + Linux only** — Skiko publishes no mingwX64 artifact.
- `window/` (publishes as `compose-desktop-native`) — what apps depend on.
  Owns `composeWindow()` and selects a renderer per target by depending on
  exactly one renderer module: mingwX64 → sdl3 (always); macOS/Linux →
  skia, or sdl3 under `-Prenderer=sdl3`. Re-exports `:core` + `:material`
  via `api`.
- `material-symbols/{outlined,rounded,sharp}/` (publishes as
  `compose-desktop-material-symbols-{outlined,rounded,sharp}`) — Material
  Symbols icon-font modules. Each ships its variable font (downloaded at
  build time from Google) and a single `MaterialSymbols{Style}` object
  with a `@Composable operator fun invoke(...)` that auto-installs the
  font on first call.
- `demo/` — flagship example app: a full showcase of the re-implemented
  Compose + Material widgets and features (30+ sidebar screens; `--gpu` /
  `--screen` / `--screenshot` CLI). Depends on `:window` + the three
  `material-symbols` styles. Not published (app only).
- `apidemo/` — a Postman-style **API Manager** built on the library: HTTP
  request collections (packs / nested sub-packs / linked copies), a
  Session → Pack → Request inheritance ladder for variables / headers /
  query-params / client-certs (innermost wins, with per-level overrides),
  syntax-highlighted JSON/XML/YAML/HTML body editors, a response viewer with
  TLS-chain inspection, mTLS client certs, drag-and-drop tree, request history
  and file-based sessions. Networking is Ktor's Curl engine (one bundled
  static libcurl per target — Schannel on Windows, OpenSSL on macOS/Linux) +
  kotlinx.serialization + okio. Not published (app only).

### How renderer selection works

Both renderer modules expose identically-signed `createRenderBackend` /
`rendererPreferredGpuMode` in the same package. `:window` has a thin
per-target `expect`/`actual` (`makeRenderBackend` / `preferredGpuMode`,
in `RenderBackendFactory.{kt,mingw.kt,macos.kt,linux.kt}`) whose actuals just
forward to those functions — and since the build links **exactly one** renderer
module per target, the call resolves unambiguously ("include one" selection).
No conditional `srcDir`s; each renderer module has its own per-OS source sets
(`mingwMain` / `macosArm64Main` / `linuxMain`) for `rendererPreferredGpuMode`.
`SDL3Backend` only exposes `COpaquePointer` (never `sdl3.*` types), so each
module declares its own `sdl3` cinterop and reinterprets — no cross-module
cinterop export needed.

### Key files (start here)

- `window/src/nativeMain/.../ComposeWindow.kt` — main loop, recomposer
  lifecycle, event dispatch; calls the per-target makeRenderBackend.
- `core/src/nativeMain/.../ComposeNativeWindow.kt` — per-window handle
  (title / size / fullscreen / rendererName / close), CompositionLocal + scope.
- `core/src/nativeMain/.../RenderBackend.kt` — the interface.
- `core/src/nativeMain/.../GpuMode.kt` — the sealed renderer/driver picker.
- `renderer-skia/.../renderer/skia/SkiaRenderBackend.kt` (+ `RenderBackendFactory.skia.kt`).
- `renderer-sdl3/.../renderer/sdl/Sdl3RenderBackend.kt` (+ `RenderBackendFactory.sdl.kt`).
- `renderer-sdl3/.../renderer/sdl/FreeTypeIcons.kt` — variable-font axis
  rasterisation for the SDL3 path (SDL3_ttf has no axis API; we go to
  FreeType directly for icon families).
- `core/src/commonMain/.../ui/node/LayoutNode.kt` — layout tree, hit testing.
- `core/src/commonMain/.../ui/Modifier.kt` — modifier elements the renderer reads.
- `demo/src/nativeMain/kotlin/Main.kt` — sidebar demo with --gpu / --screen / --screenshot CLI.
- `apidemo/src/nativeMain/kotlin/Main.kt` — the API Manager (`App()` + every
  UI panel). Siblings: `Model.kt` (serializable packs / requests / certs),
  `Persist.kt` (session + app-state IO), `Http.kt` (Ktor request runner),
  `CurlMtls.kt` (client-cert / TLS-chain via libcurl), `Packs.kt`
  (import/export), `SyntaxHighlight.kt` (body/format tokenizers).

## Build / Run

```bash
# macOS Apple Silicon, default Skia (Metal on macOS, OpenGL on Linux)
./gradlew :demo:runDebugExecutableMacosArm64
./gradlew :apidemo:runDebugExecutableMacosArm64

# Linux x64
./gradlew :demo:runDebugExecutableLinuxX64
./gradlew :apidemo:runDebugExecutableLinuxX64

# Windows
gradlew.bat :demo:runDebugExecutableMingwX64
gradlew.bat :apidemo:runDebugExecutableMingwX64

# Skiko-free build on macOS/Linux — SDL3 renderer everywhere
./gradlew :demo:runDebugExecutableMacosArm64 -Prenderer=sdl3
```

## System dependencies

### macOS (default Skia build)

`brew install sdl3` is enough. `sdl3_ttf` only needed if you set
`-Prenderer=sdl3`. Skiko klibs come from Maven (`org.jetbrains.skiko:0.150.0`).

### Linux (default Skia build)

`sudo apt install libsdl3-dev` is enough. Same caveat for SDL3_ttf.

### Windows (mingwX64 — always uses SDL3 + SDL3_ttf + SDL3_image + FreeType)

On Windows these four libraries (plus the image codecs, and a static libcurl
for `:apidemo`) are **linked statically into the executable** — the
distributable is just `<app>.exe` + `data.kres`, **no runtime DLLs**. They are
not downloaded as prebuilt binaries; they are **built from source as static
libs** by the scripts in `tools/` into a gitignored, in-repo `libs/` folder:

```bash
# From Git Bash on Windows. Needs: git, cmake, a mingw-w64 gcc/g++ on PATH,
# plus curl + python (to fetch ninja when absent). Idempotent — re-runnable.
tools/build-all.sh
```

`build-all.sh` runs `build-freetype.sh` → `build-sdl3.sh` →
`build-sdl3-image.sh` → `build-sdl3-ttf.sh` in that order (later libs link the
earlier ones), installing each as a static `.a` under:

```
libs/FreeType/{include,lib}
libs/SDL3/{include,lib}
libs/SDL3_image/{include,lib}      # vendored PNG/JPG/SVG/WEBP
libs/SDL3_ttf/{include,lib}        # carries our variable-font axis patch
```

How the build wires that up:

- **Include paths** — each module's `build.gradle.kts` injects a host-side
  `-I<repo>/libs/SDL3/include` into the cinterop on Windows (`vHostSdlInclude`);
  the `.def` files themselves only carry the macOS/Linux system paths
  (`/opt/homebrew`, `/usr/include`) and a `# mingw_x64: static-linked` note.
  `sdl3.def` is duplicated in every module that touches SDL — `core`,
  `renderer-sdl3`, `renderer-skia`, `window` (each
  `src/nativeInterop/cinterop/`); `sdl3_ttf.def` + `sdl3_image.def` +
  `freetype.def` live only in `renderer-sdl3`.
- **Linking** — `demo/build.gradle.kts` and `apidemo/build.gradle.kts` add the
  static `linkerOpts` for mingwX64: `-L<repo>/libs/.../lib`, a
  `-Wl,--start-group … --end-group` around the circular static deps
  (`ttf ↔ freetype ↔ SDL3`, `image ↔ png/webp/zlib`), the Windows system libs
  SDL3 needs when static, and `-Wl,--gc-sections -Wl,-s` to shrink + strip.
  `:apidemo` also links `-lcrypt32` for its mTLS cert-store path.

`data.kres` (STORED, no compression) is bundled next to every binary by the
per-(variant × target) `copy*ComposeResources*` Zip tasks — drawables, files,
the default `font/Roboto-Regular.ttf` the text renderers load at startup, plus
each depended `material-symbols` style font. Pass `-PbundleDefaultFont=false`
to ship without the bundled Roboto (the renderers then fall back to a system
font).

FreeType is used by the SDL3 renderer for variable-font axis support on
Material Symbols icons (SDL3_ttf has no axis-set API; we go directly to
FreeType for those families — see `FreeTypeIcons.kt`).

### Build errors on Windows

`fatal error: 'SDL3/SDL.h' file not found` or `cannot find -lSDL3` at the
cinterop / link step means `libs/` is empty or incomplete — run
`tools/build-all.sh` (from Git Bash) to build the static deps into `libs/`,
then rebuild.

## Native dependency tooling

- `tools/` — Bash scripts (`build-all.sh`, `build-sdl3.sh`,
  `build-sdl3-ttf.sh`, `build-sdl3-image.sh`, `build-freetype.sh`) that build
  the Windows native deps from source as static libs into `libs/`. Not Gradle
  modules.
- `libs/` — gitignored output of the above (per-host static libs +
  headers); referenced by the build via `${rootDir}/libs`.
- `scripts/subset-material-symbols.py` — scans an app's Kotlin sources for
  `MaterialSymbols.<Name>` usages and writes a codepoint list so the icon
  fonts can be hb-subset down to only the glyphs used (`-PsubsetIcons=true`).

## Architecture Notes

### Resources / images (composeResources)

Drop assets under `demo/src/nativeMain/composeResources/` — `drawable/` for
images (png / jpg / svg / android `<vector>` xml), `files/` for raw bytes.
The `generateComposeResAccessors` Gradle task scans that tree and emits typed
`Res.drawable.<name>` (→ `Painter`) and `Res.files.<name>` (→ path string for
`Res.readBytes`). `:core` keeps its default font under
`core/src/nativeMain/composeResources/font/`; both roots merge into
a single `<exe>/data.kres` at build time (STORED, no compression —
see `-PbundleDefaultFont`). At runtime `ResourceIO.kt` opens that archive
once via `SDL_GetBasePath()`, parses its central directory, and serves each
entry with an `fseek` + `fread` on demand — no whole-archive memory load.
The official Compose resources runtime can't be used here — its generated
code needs real Compose UI (`Painter` / `ImageBitmap` / `ImageVector`), which
this repo re-implements — so this is a self-contained stand-in.

Image drawing mirrors text exactly: a commonMain `ImageLoader` interface +
`currentImageLoader` global (set in `ComposeWindow` from
`renderBackend.imageLoader`), a `painter` leaf on `LayoutNode`, and each
renderer paints it. Decoding is per-backend and cached by path —
`SkiaImageCache` (`Image.makeFromEncoded` / `SVGDOM` on raw bytes) and
`Sdl3ImageCache` (`IMG_Load_IO` / `IMG_LoadSVG_IO` from an in-memory
`SDL_IOFromConstMem` stream). SVG and Android `<vector>` XML both flow
through `AndroidVectorToSvg` → SVG → rasterise. `ContentScale` (Fit / Crop /
FillBounds / Inside / None) and `alpha` apply at draw time. Intrinsic pixel
size is treated as logical points by the layout pass.

Fonts follow the same byte-based path: Skia uses `FontMgr.makeFromData`,
SDL3_ttf uses `TTF_OpenFontIO`. The bundled font bytes are copied once into
a `nativeHeap` allocation that lives for the renderer's lifetime so the
SDL_IOStream's reads stay valid for every opened size (closed in `destroy()`).

### Compose runtime integration (ComposeWindow.kt)

- Composables build a `LayoutNode` tree via `NodeApplier`
  (`AbstractApplier<LayoutNode>`).
- `Recomposer.runRecomposeAndApplyChanges()` runs as a child coroutine
  of the `runBlocking(frameClock)` in `composeWindow`.
- `SDL3FrameClock` is a `MonotonicFrameClock` driven by
  `frameCh.trySend(...)` once per main-loop iteration.
- **Snapshot apply notifications**: `Snapshot.sendApplyNotifications()`
  must be called each frame (or via a `registerGlobalWriteObserver`
  handler) — without it, `mutableStateOf` writes from click handlers
  never reach the recomposer.

### Layout pipeline (per frame)

1. Poll SDL events → `AppEvent` list (Quit / Pointer / Key / TextInput /
   WindowResized / MouseWheel).
2. Dispatch pointer events (hit-test the tree, walk up for
   click/hover/press/drag handlers).
3. `frameClock.sendFrame()` + `yield()` so the recomposer applies
   changes.
4. `backend.updateWindowSize()` then
   `renderBackend.ensureSize(pixelWidth, pixelHeight)`.
5. `rootNode.measure(Constraints.fixed(windowWidth, windowHeight))`
   then `rootNode.place(0, 0)` — both in **logical points**.
6. `renderBackend.beginFrame(pixelDensity)` — DPR-scales the canvas /
   SDL renderer so logical layout maps 1:1 to physical pixels.
7. `renderBackend.draw(rootNode)`.
8. `onFrame(renderBackend, frameIndex)` hook (used by --screenshot).
9. `renderBackend.endFrame()`.

### HiDPI

The window has `SDL_WINDOW_HIGH_PIXEL_DENSITY` set. `SDL3Backend`
tracks both **logical** (`windowWidth/Height`, from `SDL_GetWindowSize`)
and **physical** (`pixelWidth/Height`, from `SDL_GetWindowSizeInPixels`)
sizes. `pixelDensity = pixelWidth / windowWidth` is `2.0` on Retina,
`1.0` on standard displays.

- Layout always runs in **logical points** — `Modifier.size(64.dp)`
  means 64 logical points.
- Render backends allocate their back buffer in **physical pixels**.
- The shared scale step happens in `RenderBackend.beginFrame(dpr)`:
  Skia does `canvas.scale(dpr, dpr)`; SDL3 does `SDL_SetRenderScale(dpr,
  dpr)` *and* opens TTF fonts at `fontSize * dpr` so text textures land
  1:1 on physical pixels.

### Modifier system

`Modifier` is a small interface with `foldIn` / `foldOut`. Elements are
data classes (`PaddingModifier`, `BackgroundModifier`, `BorderModifier`,
`SizeModifier`, `ClickableModifier`, etc., all in `ui/Modifier.kt`).
Layout pulls values via `foldIn` (e.g. `node.paddingLeft`). Renderers
walk the same chain to draw background, border, and apply clip.

### Text measurement

The shared `TextMeasurePolicy` calls into a `TextMeasurer` interface
that the active `RenderBackend` provides (`SkiaTextRenderer` →
`vFont.getStringGlyphs() + vFont.getWidths().sum()`; `Sdl3TextRenderer`
→ `TTF_GetStringSize` with `length=0` so it strlens the UTF-8). Both
opt-in to subpixel measurement so layout matches drawn glyphs.

### GpuMode (sealed)

```kotlin
sealed class GpuMode {
    object Auto                 // resolve per-target (preferredGpuMode())
    object None                 // Skia CPU raster
    sealed class Skia {
        object OpenGL           // Skia + SDL3 GL context (Linux default)
        object Metal            // Skia + CAMetalLayer (macOS default)
    }
    sealed class Sdl3 {
        abstract val driverHint: String?  // for SDL_HINT_RENDER_DRIVER
        object Auto             // SDL3 picks its own driver (Windows default)
        object Software, OpenGL, Metal, Vulkan, D3D11, D3D12
    }
}
```

`makeRenderBackend(sdl, mode)` is `expect`/`actual` per source set.
Each actual rejects unsupported modes with an error so the user gets a
clear "Skia.Metal isn't available in this build" instead of silent
fallback.

### ComposeNativeWindow

Reactive handle on the window. Available two ways:

```kotlin
composeWindow(...) {
    // `this: ComposeWindowScope` — root scope
    window.setTitle("Hello")
}

@Composable
fun Deep() {
    val w = LocalComposeNativeWindow.current
    Text("Renderer: ${w.rendererName}")     // recomposes when it changes
    Button(onClick = { w.toggleFullscreen() }) { ... }
}
```

State (snapshot-backed): `width`, `height`, `pixelWidth`,
`pixelHeight`, `title`, `isMinimized`, `isMaximized`, `isFullscreen`,
`pixelDensity`, `gpuMode`, `rendererName`. Actions: `setTitle`,
`setSize`, `minimize`, `maximize`, `restore`, `setFullscreen`,
`toggleFullscreen`, `raise`, `close`.

`rendererName` calls `SDL_GetRendererName` for SDL3 modes so the live
driver string ("metal", "opengl", "direct3d11", …) shows what
`Sdl3.Auto` resolved to.

## Conventions

Follow `~/.claude/CLAUDE.md`:

- Constants: `k` prefix camelCase (`kSomeConstant`).
- Local variables: `v` prefix (`vParts`, `vResult`).
- Function parameters: `in` prefix (`inPath`).
- Class member fields in Java only: `f` prefix.
- Indent with tabs (this repo uses 4-space indent in existing code —
  match what's already in each file rather than reflowing).
- Section headers in Kotlin:
  - Major sections (file-level / between classes):
    ```kotlin
    // ==================
    // MARK: Name
    // ==================
    ```
  - In-function smaller scope:
    ```kotlin
    // ============
    //  Name
    ```
- Concise function-level comments only where the name is not
  self-documenting; avoid line-by-line commentary.
- Kotlin standard syntax — no Spirtech internal rules apply here.

## Common Pitfalls

- **State changes don't update the UI** — verify
  `Snapshot.sendApplyNotifications()` is being called each frame in
  `ComposeWindow.kt`. The recomposer is otherwise idle.
- **Text appears clipped on the right** — measurement must match the
  glyphs the renderer actually paints. In Skia this means
  `getStringGlyphs() + getWidths().sum()` (not `measureTextWidth`,
  which is broken in Skiko 0.150.0 Native). In SDL3 it means passing
  `0` to `TTF_GetStringSize / TTF_RenderText_Blended` so it strlens
  the UTF-8 — passing `inText.length` truncates non-ASCII strings.
- **Retina blur** — make sure the back buffer is at pixel size and the
  canvas / `SDL_SetRenderScale` applies the DPR. Text textures via
  SDL3_ttf must be opened at `fontSize * dpr` or they upsample blurry.
- **Row with `Arrangement.spacedBy(...)`** — `RowMeasurePolicy` adds
  inter-child gaps to its reported width; if you change it, make sure
  centering in a parent still works.
- **`-Prenderer=sdl3` mode** — flips `:window`'s macOS/Linux
  dependency from `:renderer-skia` to `:renderer-sdl3`; the Skia
  module just isn't on the dependency graph (not compiled, Skiko not pulled).
  Don't add a hard dependency on `:renderer-skia` from a shared source set,
  or mingwX64 (which has no Skia module) won't link.
- **mingwX64 cross-compile from macOS / Linux fails** at the cinterop
  step — it can't find `<repo>/libs/SDL3/include/SDL3/SDL.h` (and the static
  libs under `libs/` are built per-host by `tools/` anyway). That's expected;
  build the Windows target on Windows.
- **Configuration cache and `-Prenderer=`** — Gradle caches the
  configuration; toggling the renderer property may not invalidate the
  cache. Delete `.gradle/configuration-cache/` between switches if you
  see weird "couldn't find sdl3_ttf" errors.

## Useful gradle tricks

- `--args="--gpu=sdl3.opengl --screen=Buttons"` to pass CLI to the demo.
- `--info` to see the cinterop classpath / include paths actually used.
- `--rerun-tasks` to force-rebuild after toggling `-Prenderer=`.
- `-PsubsetIcons=true` (default on, see `gradle.properties`) —
  `scripts/subset-material-symbols.py` scans an app's sources for
  `MaterialSymbols.<Name>` uses and hb-subsets each bundled icon font to only
  the glyphs actually used (needs `python3` + `hb-subset` on PATH; falls back
  to the full font if `hb-subset` is absent).

## License

MIT — see [LICENSE](LICENSE.md).
