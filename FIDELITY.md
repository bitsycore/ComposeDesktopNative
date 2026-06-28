# Compose-fidelity handoff

Status + how-to-continue for the ongoing pass that makes core's
`androidx.compose.{foundation,ui,animation}` surface mirror official Compose.
Read this with the **Compose API Fidelity** section of `CLAUDE.md` (rules) — this
file is the *current state*; some "known-diverging / TODO" notes in CLAUDE.md are
now done (see below).

## Strategy (the rule we follow now)

- **Reusable-from-upstream low-level types → vendor verbatim**, kept in their
  official `androidx.compose.*` package (Dp, Color, Offset, TextUnit, …).
- **Anything with no official public equivalent → relocate to
  `com.compose.desktop.native.*`** (the render-bridge / engine / custom desktop
  API). The official extension that *builds* it stays in `androidx.*`.
- Match upstream signatures/representation where a type is official-named.

## The vendor pipeline — `tools/compose-fork/`

- `compose-ref.txt` — pinned upstream commit of JetBrains/compose-multiplatform-core
  (currently `1be9d64a` = `v1.12.0-beta01+dev4324`).
- `manifest.txt` — `<upstream-path>  <dest-under-repo>`; the list of verbatim-vendored files.
- `sync.sh` — clones the ref (sparse: ui/foundation/animation) to `$CMP_REF` or
  `../cmp-ref`, then copies each manifest file **byte-for-byte verbatim** into
  `core/src/vendor/{common,native}/kotlin/`. Idempotent; `git diff` after a
  re-sync shows upstream drift. **Never hand-edit `core/src/vendor/**`** — see its README.

### On macOS (first time)

```bash
# clone-or-reuse the upstream ref + (re)vendor; clone lands at ../cmp-ref
tools/compose-fork/sync.sh
# fidelity check (auto-finds ../cmp-ref now; or pass a path / set CMP_REF)
./gradlew apiDump && python3 scripts/compose-fidelity-check.py
# run the apps on the default Skia renderer
./gradlew :apidemo:runDebugExecutableMacosArm64
./gradlew :demo:runDebugExecutableMacosArm64
```

## ⚠️ Verify on macOS (Skia path was unreachable on Windows)

`renderer-skia` is **not** in the mingwX64 build graph, so every edit below was
compile-checked only via the identical sdl3 change — macOS is the first place
Skia actually compiles + runs. Launch `:demo` and `:apidemo` and check:

- text renders at the right **size + alignment** (TextUnit + the `TextAlign` `else`
  branches added in `SkiaTextRenderer`)
- **colours** correct (vendored `Color` = packed sRGB value class; `r8/g8/b8/a8`
  are now extensions imported in `SkiaTextRenderer`)
- **strokes** (canvas screen) — `StrokeCap` value class + `else` in `SkiaDrawScope`
- **graphicsLayer / alpha / transforms** + general layout (`GraphicsLayerModifier`,
  `LayoutNode` moved packages; renderer-skia imports updated)
- **selection**: open a large JSON response body in apidemo, drag-select, Ctrl/Cmd+C
  (the selection-aware BasicText fix)

All of the above already verified working on Windows/SDL3.

## Done (divergence 913 → 589)

- **Vendored verbatim** (0 divergence): `ui.util`, `ui.geometry`, all `ui.unit`
  value types (Dp/TextUnit/Constraints/Int*/Velocity/Dp{Offset,Size,Rect}),
  `ui.graphics.Color` + the whole `colorspace` subsystem + Float16, `TileMode`,
  `StrokeCap`, `ui.text.TextRange`, `TextAlign`/`TextOverflow`,
  `FontStyle`/`FontWeight`. (`Sp` was migrated to the real `TextUnit`.)
- **Relocated to `com.compose.desktop.native.*`** (no official equivalent):
  the ~22 `Modifier.Element` classes + `GraphicsLayerModifier` → `.element`;
  `LayoutNode` + `NodeApplier` (+ internal node `MeasurePolicy`) → `.node`;
  `ScrollAnimator` → `.scroll`; Popup host infra → `.window`; `ColorRun` +
  `SelectableText`/`LocalInSelectionContainer` (folded into selection-aware
  BasicText) → `.text`; `InfiniteTransition.animateDp` → `.animation`.
- **Reshaped to match official**: `PaddingValues` → interface
  (`calculate*Padding` + `calculateStart/EndPadding`); `ToggleableState`→`ui.state`,
  `PaddingValues`→`foundation.layout` placement.
- **Kept in androidx as documented exceptions**: `Clipboard` + `currentClipboard`,
  `currentImageLoader`, `currentTextMeasurer` (mutable backend-wiring globals,
  same pattern); the `ui.res` `Res`/`ImageLoader` stand-in.

## Remaining (per the relocate-or-match directive)

**Safe relocations → native.\*** (pure package moves, the proven pattern):
- `ui.text` render glue: `TextMeasurer` / `WrappedText` / `TextRendererCapabilities`
  + `currentTextMeasurer` / `currentViewportHeight/Width` → `native.text`
- `ui.input` render-bridge: `PointerInputElement` / `PointerInputEvent` /
  `PointerInputScopeImpl` / `KeyModifiers` → `native.input`
- `ui.graphics` `PathCommand` → native; `foundation` `ScrollbarAdapter` → native
- (bigger, app-facing) the `ui.res` system (`Res`/`ImageLoader`/`ResourceKind`/…)

**Match-upstream reshapes**: `SpanStyle`/`TextStyle`/`ParagraphStyle` + the
animation specs (`SpringSpec`/`TweenSpec`/…) `data class` → plain class with
manual equals/hashCode (drop `component*`/`copy`); `BorderStroke` → wraps a `Brush`.

**Runtime-critical (do last, screenshot-test)**: `KeyEvent` / `PointerEvent` /
`PointerEventType` / `PointerButton` enum/data-class → official value classes —
touches the live input path (SDL3EventMapper + ComposeWindow + BasicTextField).

**Out of scope**: `material` (no cmp-ref material clone), the intentional-custom
`AnimationSpec` lerp-lambda design, `ScrollState`/`Lazy` suspend reshapes.

## Verifying / re-checking

`./gradlew apiDump` regenerates `*/api/*.klib.api`; `apiCheck` guards drift.
`python3 scripts/compose-fidelity-check.py` lists our `androidx.compose.*`
decls not in upstream (the divergence number above). The big remaining buckets
are `material` (294, out of scope) + the surface-match/reshape items above.
