# RENDERER_CONVERGE.md

Plan for converging this port's rendering onto upstream Compose/Skiko internals —
"same internals as upstream, or as close as possible" — and for restoring the
upstream module boundaries (`:ui-graphics`, `:ui-text`) that are currently merged
into `:ui`.

The single living renderer doc — the forward plan up top, plus **Appendix A** (the
retained-layer engine model + what already landed, consolidated from the retired
`RENDERER_REFACTOR.md`). **Revised after a 3-agent review** (feasibility / upstream-
fidelity / risk-sequencing) — see §13 for the synthesis; the revisions are folded in
below.

---

## 0. RULES (read first — these govern every decision below)

Non-negotiable principles. If a step violates one, the step is wrong, not the rule.

1. **Vendor as much as possible.** Copy upstream Compose / Skiko code verbatim into
   `src/vendor/` via `compose-fork.txt`. Hand-roll nothing upstream already provides.
   Edit-to-compile → copy-with-comment (manual vendoring). Never reinvent.

2. **Common Rendering layer, based on upstream Skiko as much as possible.** The shared,
   renderer-agnostic engine mirrors upstream's `skikoMain` structure and *mechanics*.
   We reuse upstream's machinery, not our own invention of it.

3. **ACTUAL only where genuinely backend-specific; commonize everything else.** Code
   goes: `Common` (upstream) → `CDN-common` (shared native engine) → `Skia actual` /
   `SDL actual` (leaf rasterization ONLY). Seam as narrow and low as possible.

4. **Use upstream mechanics so our internals END UP identical to upstream** (or super
   close). Prefer the exact upstream class/algorithm over a lookalike.

5. **Restore upstream module boundaries.** `:ui-graphics` and `:ui-text` are their own
   modules, not merged into `:ui`.

**Litmus test:** *"Is this what upstream does? If not, what real platform constraint
forces the difference?"* Valid answers name a constraint (no Skiko on Windows K/N; SDL
is a triangle blitter). "It was easier" is not valid.

> **A caveat on Rule 4, surfaced by review:** upstream has **no renderer-agnostic node
> abstraction** — it uses skiko's concrete `RenderNode` directly, with no intervening
> interface. Our `NativeRenderNode` is a **port invention justified solely by the second
> (SDL) backend** (Rule 3). It is *not* an "upstream mechanic to converge toward"; on the
> Skia leg the goal is to **shed** it in favor of upstream's own `GraphicsLayer`/
> `RenderNode` (see B2). Keep this straight throughout.

---

## 1. NORTH STAR (revised for feasibility)

> Rendering is **upstream Compose/Skiko, vendored**, wherever the real Skia library
> exists — so on macOS/Linux the internals *are* upstream, not merely close. On Windows,
> where no Skia is available to Kotlin/Native, a **permanent SDL backend** implements the
> same seam and is converged as close as the backend allows. The two backends share the
> maximum common engine and diverge only at leaf rasterization. `:ui-graphics` and
> `:ui-text` are separate modules exactly as upstream ships them.

The review established that **"real Skia on *Windows*" is probably not reachable without
breaking a hard project invariant** (§3). So the honest north star is: **literally
upstream on macOS/Linux + a first-class, converged SDL renderer on Windows** — not "real
Skia everywhere." The just-shipped SDL geo node is therefore an **asset to invest in**,
not debt to delete.

---

## 2. CURRENT STATE (what we converge FROM)

- **Shared engine landed** (`nativeMain`, renderer-agnostic): `GraphicsLayerOwnerLayer`,
  `GraphicsLayer.native`, `NativeRenderNode` (interface + `expect createNativeRenderNode`),
  `DeferredRenderNode`, `ComposeOwner`, `ComposeRootHost`, `LayerTransformationMatrix`,
  `SdlParagraph.native`, `TextMeasurer`. Three-level skip in place. See Appendix A.
- **SDL leg shipped as the Windows default and verified** (this is new — treat as an
  asset): the cached-geometry `geo` node (`SdlDisplayListRenderNode`) is default, 0.000%
  across a 57-screen sweep bar a <0.06% cosmetic rotated-edge AA fringe, capturing
  geometry + plain/spanned text + icon glyphs; `CDN_LAYERCACHE=off|texture` fallbacks.
  ~3,800 lines; the perf win the refactor targeted is realized.
- **Skia leg** (`skikoRendererMain`, macOS+Linux): `SkiaCanvas` is a thin ~504-line bridge
  to `org.jetbrains.skia.*`. Its `createNativeRenderNode` still returns `DeferredRenderNode`
  — **not** skiko's `RenderNode`. Its `GraphicsLayer` is the port's hand-rolled
  `GraphicsLayer.native`, **not** upstream's `actual class GraphicsLayer`.
- **`compose-fork.txt` currently REFUSES (`!`) three vendorable skiko files** the Skia leg
  could take verbatim: `SkiaGraphicsLayer.skiko.kt`, `SkiaGraphicsContext.skiko.kt`,
  `GraphicsLayerOwnerLayer.skiko.kt`. The port hand-rolled equivalents instead — a Rule-1/2
  gap (see B2).
- **Module merge**: `:ui` contains `ui-graphics` + `ui-text` because their `Canvas` /
  `Paragraph` `expect`s must co-locate with their renderer `actual`s.
- **Hard constraints**: (i) **Skiko publishes no `mingwX64` K/N klib**; (ii) **the port
  ships no runtime DLL** — distributable = `<app>` + `data.kres`. Both bear on §3.

---

## 3. TRACK A SPIKE — can we get REAL Skia on Windows K/N? (rewritten; likely NEGATIVE)

Review consensus: **the direction is right to gate, but the honest expected outcome is
"no, not without a runtime DLL."** Do the spike anyway — it's cheap if front-loaded on
the kill-shot — but pre-decide the DLL tradeoff so a negative result is a clean shelving,
not a sunk cost.

**Why (verified by review):**
- Skiko already bridges Skia's C++ to K/N via hand-written `extern "C"` glue + cinterop on
  all its native targets — so **"can K/N call Skia" is solved**; `extern "C"` glue is
  *mandatory in every route* (cinterop is C-only). The unknown is purely the **Windows
  build/ABI**.
- **Kotlin/Native `mingwX64` is GNU-ABI** (mingw-w64 sysroot). **skia-pack / JetBrains-skia
  Windows is MSVC-ABI** (requires clang-cl). MSVC-ABI C++ **cannot be statically fused**
  into a GNU-ABI K/N binary (mangling, exception model, C++ runtime differ).
- The real crux is therefore: **can *current* Skia (C++20) be built to a GNU/mingw-ABI
  static `.a` at all?** Skia's own toolchain forces MSVC/clang-cl on Windows; the only
  precedent for mingw Skia is **Mozilla's tier-3 patches from the m55–m70 era**, largely
  abandoned. Against C++20 m138 this is an open-ended fork. **RED risk; likely infeasible.**
- **The Linux native precedent does NOT de-risk Windows.** Linux has one dominant C++ ABI
  (Itanium/libstdc++) shared by gcc+clang, so K/N-linux ↔ Skia-linux "just links." Windows
  is uniquely hard *because* of the GNU-vs-MSVC schism. Do not argue "Linux works → Windows
  is just another target."

**Routes (corrected):**
- **(a) Extend skiko's native build to `mingwX64`** — reuse skiko's whole glue/cinterop
  surface; needs a GNU-ABI Skia (same crux below). Most upstream-faithful *if* the archive
  exists.
- **(b) MSVC/clang-cl-built Skia + `extern "C"` shim as a **DLL***, consumed by K/N mingw
  across the C boundary via the import lib. **This is the reliable, standard MSVC↔mingw
  path — and it works — BUT it ships a runtime DLL, violating the no-DLL invariant.** A C
  shim resolves the ABI mismatch *only at a DLL boundary*; it does **not** make a static
  MSVC-ABI Skia statically linkable. (This corrects the prior draft, which called the shim
  a static alternative.)
- **(c) Skia `sk_capi`** — experimental, pre-1.0, incomplete; skiko deliberately avoids it.
  Confirm-and-discard cheaply.

**Re-scoped spike (kill-shot first, no Kotlin until it passes):**
1. **Experiment #1 — the kill-shot:** produce a **GNU-ABI static `libskia.a`** for the
   pinned Skia version. Try, in order, time-boxed ~2–3 days: **(i) clang targeting
   `x86_64-w64-windows-gnu`** (NOT clang-cl — keeps clang so Skia's SW rasterizer stays
   fast; the route the prior draft omitted); **(ii)** mingw-w64 GCC + the Mozilla patch
   set. **If neither yields a K/N-mingw-linkable archive, static Track A is dead — stop.**
2. **Experiment #2 (only if #1 passes):** compile a tiny `extern "C" skia_test_rect()`
   glue with the *same* toolchain, cinterop it from a throwaway `mingwX64` K/N binary, and
   raster to a **CPU `SkSurface`** (no GL). Validates glue-ABI + cinterop + lifetime.
   Reuse skiko's glue sources (route (a) done right).
3. **Default present path = CPU raster → `SDL_UpdateTexture`/`SDL_RenderTexture`** (infra
   the SDL leg already has). This **isolates the interop risk from the Windows-GL risk**:
   raw desktop GL is historically flaky on Windows — skiko uses **ANGLE/D3D** there, which
   would pull in ANGLE (~10–20 MB, normally DLLs). Add a GPU present path *only if* CPU
   raster measures too slow (the retained engine already re-rasters only dirty layers, so
   full-window cost is bounded).
4. **DLL decision, decided NOW:** if #1 fails, the only working real-Skia-on-Windows is the
   **route (b) DLL**. Is shipping one runtime DLL acceptable? If **no** (current invariant),
   **Track A is shelved on Windows and Track B is the permanent ceiling.** Wire into §11.

**Binary size (honest):** a full Skia (codecs + GPU) is **~20–40 MB**, +ANGLE if GL. Track
A *deletes* a kilobytes-scale hand-rolled rasterizer and *adds* tens of MB **on Windows —
the only platform Track A targets.** That is a distribution regression, not a wash.

---

## 4. TRACKS

### Track B — PRIMARY (do now; the real convergence path)

Track B is not "interim." Given §3's likely-negative outcome, Track B is *the* path: it
makes macOS/Linux literally upstream and keeps the SDL renderer as a permanent, converged
Windows backend. Ordered by value × spike-independence:

- **B2 — Skia leg: VENDOR upstream's `GraphicsLayer`, don't wrap. (Highest-value, do now,
  CI-buildable, spike-independent.)** Un-refuse `SkiaGraphicsLayer.skiko.kt` +
  `SkiaGraphicsContext.skiko.kt` (+ `GraphicsLayerOwnerLayer.skiko.kt` if the owner arch
  allows) in `compose-fork.txt`; carry upstream's `actual class GraphicsLayer(renderNode:
  skiko.RenderNode)` **verbatim** on `skikoRendererMain`, retiring the port's
  `GraphicsLayer.native` + `NativeRenderNode` *on the Skia side*. `GraphicsLayer` is an
  `actual class` and the two renderer source sets attach to **disjoint targets** (proven by
  `createRenderBackend`), so the SDL leg keeps its own actual. Result: **the platforms where
  Skia already exists become literally upstream** — the North Star, no spike needed. This
  supersedes the prior "wrap skiko RenderNode behind our façade" (that was *less* vendoring).
- **B5 — Common-engine convergence.** Audit CDN-common vs upstream `skikoMain`; vendor the
  deltas (see §8). Spike-independent.
- **B1 — SDL-only node dedup (re-scoped).** A base for the two **SDL** nodes
  (`DeferredRenderNode`* + `SdlDisplayListRenderNode`) sharing the "SDL is a triangle
  blitter" transform/shadow/clip scaffolding. **Lives in `sdlRendererMain`, NOT CDN-common,
  and explicitly does NOT bind the skiko node** (which delegates to skiko per B2). This is a
  real SDL constraint (Rule 3), not an upstream mechanic. Low-risk mechanical dedup, but it
  now touches the *newly-default* geo hot path → run it against a fresh parity+probe+perf
  baseline. *(\*`DeferredRenderNode` is the shared default node; after B2 it is used only by
  the SDL leg + as the SDL fallback, so co-locating the base in the SDL set is consistent.)*
- **B3 — SDL fidelity, parity-ranked.** Gamma-correct gradients, AA quality, `RenderEffect`/
  blur, complex-script text, true layer compositing. **Not throwaway** now that Track A is
  likely shelved (the SDL renderer is permanent). Still cap to parity-ranked wins so effort
  tracks visible impact.
- **B4 — SDL perf.** Mostly shipped (see Appendix A.3). Remaining: shared glyph atlas
  (replace per-run-string textures), `drawImageRect` capture (journal rates low-ROI — as
  needed). Do opportunistically.

### Track A — GATED, LIKELY-NEGATIVE (spike per §3)

Only if the spike passes **and** the DLL tradeoff is accepted:
- Windows gains real Skia (present-only SDL; CPU-raster present by default). macOS/Linux
  keep their direct Skia GPU path (no present-only regression there).
- **Do NOT delete the SDL rasterizer.** The prior draft's "delete it" contradicted "keep
  `-Prenderer=sdl3`" (a Skiko-free build *is* the rasterizer). Resolution: the **SDL geo
  node stays the default / small-binary / no-DLL Windows path**; real Skia is an **opt-in
  max-fidelity build**. Two configs, chosen deliberately — the rasterizer is not debt.

### Non-goal (recorded): "abstract + re-back with SDL" does not shrink SDL work

The Skia leg is a thin bridge; its substance is Skia's C++, not copyable Kotlin. The "layer
in between" already exists (`androidx.compose.ui.graphics.Canvas`, with the pipeline above
it shared). Re-seaming at `org.jetbrains.skia.Canvas` reproduces the Canvas interface and
leaves the SDL impl equally thick. SDL thickness is the cost of SDL being a triangle
blitter. The only way to *reuse* the Skia renderer is real Skia (Track A).

---

## 5. SEQUENCING (revised)

```
Phase 0 (now, parallel)   • Spike §3 kill-shot (Exp #1 GNU-ABI libskia.a) — time-boxed 2-3d.
                          • Stand up the Linux Skia-leg CI parity job (prerequisite for
                            verifying B2/B5/module-split — NOT an afterthought).
Phase 1 (now, spike-      • B2 (vendor upstream GraphicsLayer, Skia leg) — biggest Rule-1/2 win.
        independent)      • B5 (engine convergence, vendor deltas).
Phase 2                   • B1 (SDL-only node dedup, against fresh baselines).
                          • B3/B4 SDL fidelity + perf, parity-ranked, as-needed.
Phase 3 (gate)            • If spike PASS + DLL accepted → Track A (real-Skia opt-in, keep
                            SDL default). Else → shelve Track A; Track B is the ceiling.
Phase 4                   • Module split §7 (trivial under Track A; relocation under Track B).
Cross-cutting             • Multi-gate verification §9 on every rendering change.
```

Rationale: B2/B5 pay off regardless of the spike and are CI-buildable now; the kill-shot
retires Track A's risk for near-zero cost; the module split lands last.

---

## 6. PART TWO — RESTORE `:ui-graphics` AND `:ui-text` AS MODULES

**Goal:** match upstream artifact boundaries. `:ui` holds only `androidx.compose.ui.*`
(minus graphics/text); `:ui-graphics` and `:ui-text` become their own modules.

**Why merged today:** `Canvas`/… (ui-graphics) and `Paragraph`/`ParagraphIntrinsics`
(ui-text) `expect`s resolve to renderer `actual`s; Kotlin requires `expect`+`actual` in the
same module; the renderers live in `:ui`.

**DAG (verified against upstream `cmp-ref` v1.12.0-beta01):**
`ui-graphics → ui-unit`; `ui-text → ui-graphics + ui-unit`; `ui → ui-graphics + ui-text +
ui-geometry + ui-unit + ui-util`. File placement upstream:
- **`ui-graphics`**: `Canvas`, `GraphicsLayer`, `GraphicsContext`, all skiko `RenderNode`
  usage (`SkiaBackedCanvas.skiko.kt`, `SkiaGraphicsLayer.skiko.kt`, `SkiaGraphicsContext.skiko.kt`).
- **`ui`**: `OwnedLayer`, `GraphicsLayerOwnerLayer`, `RootNodeOwner`, `LegacyRenderNodeLayer`,
  `LayoutNode`.
- **`ui-text`**: `Paragraph`, `ParagraphIntrinsics`.
"Put each file where upstream puts it" → no cycle (matches upstream).

**Cinterop placement (corrected — this was self-contradictory in the prior draft):**
`SdlParagraph.native.kt` is **cinterop-free** (it goes entirely through the
`com.compose.sdl.text.*` interface seam — `currentTextMeasurer`, `NativeTextCanvas`,
`NativeTextMeasurer`, already in `commonMain`). The text renderers (`Sdl3TextRenderer`,
`FreeTypeIcons`) DO use `sdl3`/`sdl3_ttf`/`freetype`, and `sdl3_ttf.def`/`sdl3_image.def`
carry `depends = sdl3`. Therefore:
> **Keep all four cinterops AND both concrete renderers in `:ui-graphics`. Move only
> `Paragraph`/`ParagraphIntrinsics` (expect+actual) + expose the `com.compose.sdl.text`
> interface to `:ui-text`.** No cinterop crosses a module boundary; the sibling
> `depends = sdl3` `-library` wiring stays intact inside `:ui-graphics`. (Answers Open Q4.)

**Track dependence:** trivial under Track A (actuals are upstream skiko's, vendored;
SDL gone from graphics). Under Track B it is pure relocation churn (+ cinterop-move risk if
done wrong — the resolution above avoids it) with **no user-facing benefit**, so **defer to
Phase 4** unless publishing-granularity demands it sooner. B2's Skia-leg vendoring (which
*reduces* the hand-rolled surface to relocate) should NOT wait, however.

---

## 7. VENDORING TARGETS (Rule 2/4 — named)

Vendor these upstream `skikoMain` mechanics into the Skia leg / CDN-common; each via
`compose-fork.txt` (pin ref, re-sync, let the build report gaps):
- **B2 core:** un-refuse `SkiaGraphicsLayer.skiko.kt`, `SkiaGraphicsContext.skiko.kt`,
  `GraphicsLayerOwnerLayer.skiko.kt`.
- **Named fidelity gaps the port currently drops** (from `GraphicsLayer.native.kt`'s own
  header): **layer outsets / blur (RenderEffect) bounds expansion** — trimmed, which shows
  up as **clipped blur** in parity; and **`ChildLayerDependenciesTracker`** — upstream's
  parent→child layer-lifetime mechanism, for which the port substitutes
  `NativeReleaseQueue`/GC. Re-vendor or consciously re-implement to match.
- Confirm `ParagraphIntrinsics`/`Paragraph`/font-resolver structure matches upstream;
  SDL keeps its `NativeTextMeasurer` impl behind the interface.

---

## 8. VERIFICATION (expanded — parity alone is NOT sufficient)

The refactor journal proves screenshots-vs-JVM missed a nav crash (the **probe** caught it)
and produced false 13–17% signals from settle-timing (Pickers). So gate on multiple, co-equal
nets:
- **`scripts/parity/parity.py`** (native-vs-JVM) — the primary *fidelity ranking* net; the
  ONLY measure of "match upstream" (geo-vs-block-replay diffs prove self-consistency, not
  upstream fidelity).
- **Probe rigs** (`scripts/probe/`) — co-equal, for interaction/crash regressions parity misses.
- **A frame-locked / deterministic parity mode** — to defeat settle-timing false signals.
- **Perf-regression gate** — a `CDN_PROFILE` `draw`-ms threshold on LazyColumn/Tabs (the
  whole effort's origin was perf; there is currently no perf gate — a hole).
- **HiDPI / Option-B density** and **multi-window** checks — exactly what a present-path or
  layer-engine change (especially Track A's present-only blit) breaks.
- **Linux Skia-leg CI parity job** — a **prerequisite**: B2/B5/module-split are not
  buildable on the Windows dev box, so without this CI they ship unverified.

---

## 9. RISKS & ROLLBACK

- **Skia-on-mingw likely infeasible** (GNU-ABI build of C++20 Skia). RED. Mitigation:
  kill-shot spike first; near-zero sunk cost on a negative; B2/B5 proceed regardless.
- **No-DLL invariant vs the only working route (b) DLL.** Pre-decided in §3.4.
- **Windows-GL flakiness** (skiko uses ANGLE/D3D) + **ANGLE size/DLLs.** Mitigation: default
  present path = CPU raster.
- **Binary-size regression on Windows** under Track A (~20–40 MB +ANGLE vs kilobytes today).
  State the baseline + projected multiple before committing.
- **B1 touches the newly-default geo hot path.** Mitigation: fresh parity+probe+perf baseline;
  keep strictly mechanical.
- **Module-split cinterop cross-boundary** — avoided by the §6 resolution.
- **Divergence drift** — Rule 4 + periodic `compose.properties` bump + re-sync.
- Everything on a branch; each phase independently revertible; multi-gate §8 is the net.

---

## 10. OPEN QUESTIONS

1. **Is real-Skia-on-Windows worth it at all**, given (a) likely-infeasible static build and
   (b) the DLL route breaks the no-DLL invariant + adds tens of MB? Default answer trending
   **no** → Track B is the permanent ceiling. Confirm via the kill-shot spike.
2. **Is one runtime DLL ever acceptable** for an opt-in max-fidelity Windows build? (If never,
   Track A is dead the moment Exp #1 fails.)
3. Should the `:ui-graphics`/`:ui-text` split happen under Track B for publishing/API reasons,
   accepting relocation churn, or wait? (Recommend wait; do B2 now regardless.)
4. ~~Module DAG~~ — **answered** in §6 (cinterops stay in `:ui-graphics`; only Paragraph →
   `:ui-text`).
5. Present path under Track A: CPU raster (default, simplest, isolates risk) vs GPU (measure
   only if CPU too slow).

---

## 11. TARGET END-STATE NODE SET (so the matrix doesn't sprawl)

- **Skia leg:** upstream `actual class GraphicsLayer` backed by skiko `RenderNode` (B2). No
  `NativeRenderNode`, no `DeferredRenderNode`.
- **SDL leg:** `SdlDisplayListRenderNode` (geo, default) + `DeferredRenderNode` (fallback),
  sharing the SDL-only base (B1). `SdlRenderNode` (texture, legacy) retired once geo is
  unquestionably dominant.
- **Track A (if it ever lands):** SDL present-only path is additive; nodes unchanged.

---

## 12. STATUS / JOURNAL

- 2026-07-16 — Doc created; revised after 3-agent review (all **APPROVE-WITH-CHANGES**).
  Key shifts folded in: Track A demoted to a likely-negative gated spike (mingw-ABI Skia
  RED; DLL route breaks the no-DLL invariant); Track B promoted to PRIMARY; **B2 reframed
  from "wrap" to "vendor upstream's `actual class GraphicsLayer`"** (un-refuse 3 skiko files
  — biggest Rule-1/2 win, spike-independent); **B1 relocated to `sdlRendererMain`, SDL-only,
  not binding the skiko node**; §6 cinterop resolution (all cinterops stay in `:ui-graphics`,
  only `Paragraph` → `:ui-text`); §8 verification expanded (probe + frame-locked + perf +
  HiDPI + multi-window + Linux CI); §7 named the outsets/blur + `ChildLayerDependenciesTracker`
  gaps; corrected the record that `NativeRenderNode` is a port invention, not an upstream
  mechanic. **Recommended first concrete step: B2 (vendor upstream GraphicsLayer on the Skia
  leg) + stand up Linux CI — both spike-independent.** Spike §3 not yet run.

---

# APPENDIX A — Retained-layer engine: model, contract, and what landed

Consolidated from the (now-deleted) `RENDERER_REFACTOR.md`. This is the durable
reference for *why* the engine is shaped as it is and *what already shipped*; the
blow-by-blow commit journal is dropped — the endpoints and learnings are kept.

## A.1 How upstream skiko renders (the model we copied)

Upstream skips work at **three levels**; we have all three now.

- **L1 — frame scheduling.** After each frame the scene only schedules another if
  still dirty (`hasInvalidations()`); invalidations arrive targeted from the snapshot
  observer. *Ours:* `ComposeWindow.shouldRender()` gates `renderFrame()`; the loop
  blocks on `SDL_WaitEventTimeout` when idle.
- **L2 — measure/layout only dirty nodes.** Vendored `MeasureAndLayoutDelegate.
  relayoutNodes` (depth-sorted, holds only nodes needing work; self-skips when clean).
  *Ours:* used verbatim via `ComposeOwner.measureAndLayout()`.
- **L3 — draw record-once / replay (the expensive one).** Each isolated `LayoutNode`
  gets an `OwnedLayer` (`GraphicsLayerOwnerLayer`) owning a `GraphicsLayer` (upstream's
  RenderNode concept). `updateDisplayList()` re-records **only if `isDirty`**;
  `drawLayer()` **replays** the cached display list.
  - **The critical property:** *transform / alpha / clip changes do NOT set `isDirty`.*
    Moving, scaling, rotating, or fading a layer just **replays cached content under a
    new transform** — no re-record. `move()` (scroll) only updates `topLeft`. Only a
    genuine content change (a state read inside the draw block, or a resize) re-records.
  - Upstream **replays the whole scene every frame** — no dirty-region / partial
    present. The entire win is from **not re-recording clean layers**, not from
    redrawing less screen. (So we explicitly do NOT need dirty-region rendering.)
  - `GraphicsLayer` (skiko actual) is a thin façade over `org.jetbrains.skiko.node.
    RenderNode` (a Skia `Picture` display list). All transforms/effects are RenderNode
    *properties* applied at replay. `LegacyRenderNodeLayer.skiko.kt` is the pure-Skia
    `PictureRecorder`/`Picture` reference — the blueprint for a from-scratch node.

Key upstream files (under `cmp-ref`): `node/GraphicsLayerOwnerLayer.skiko.kt` (the
OwnedLayer — dirty-gated record+replay+property-diff), `OwnedLayerManager` impl in
`RootNodeOwner.skiko.kt` (`dirtyLayers`, `notifyLayerIsDirty`, `recycle`),
`node/LegacyRenderNodeLayer.skiko.kt` (pure-Skia reference), `ui-graphics/.../layer/
SkiaGraphicsLayer.skiko.kt` (GraphicsLayer actual), `ui-graphics/.../SkiaGraphicsContext
.skiko.kt`, `ui-graphics/.../SkiaBackedCanvas.skiko.kt`.

## A.2 Compositing-strategy contract (`requiresLayer()` — match on both legs)

From `SkiaGraphicsLayer.skiko.kt`. Getting this right is what makes overlapping-content
alpha, tinted layers, and blend modes correct without over-allocating offscreens:

| Condition | `Auto` | `Offscreen` | `ModulateAlpha` |
|---|---|---|---|
| `alpha < 1` | offscreen | offscreen | per-op alpha multiply (no offscreen) |
| `colorFilter != null` | offscreen | offscreen | offscreen |
| `blendMode != SrcOver` | offscreen | offscreen | offscreen |
| `renderEffect != null` | offscreen | offscreen | offscreen |
| none of the above | replay in place | always offscreen | replay in place |

`ModulateAlpha` bakes alpha into recorded ops at record time (alpha changes re-record).

## A.3 What landed (the engine + the SDL geo node)

**Retained-layer engine (shared, `nativeMain`):** `createLayer` returns
`GraphicsLayerOwnerLayer` (vendored from skiko, `setLightingInfo` tail stripped);
`ComposeOwner` implements `OwnedLayerManager` (`dirtyLayers` + `notifyLayerIsDirty` +
`recycle` + `voteFrameRate`); `invalidate()` → window `needsFrame`; `renderRoot()`
re-records dirty layers then walks the tree. `GraphicsLayer.native` is a copy-edit of
`SkiaGraphicsLayer.skiko.kt` — a façade over `NativeRenderNode` (transforms trimmed of
outsets + `ChildLayerDependenciesTracker`). Drives entirely through the vendored
`NodeCoordinator`/snapshot observer — no changes there.

**SDL node evolution (all behind `CDN_LAYERCACHE`, geo now default):**
1. **`DeferredRenderNode`** (shared, `nativeMain`) — replay-the-block; the correct,
   un-cached baseline; still the fallback (`=off|defer|0`).
2. **`SdlRenderNode`** (texture, `=1|texture`, LEGACY) — records a leaf into an
   offscreen texture, replays as a blit. Fast for static leaves BUT **nondeterministic
   on complex screens** (offscreen-texture timing state; reused targets ghosted until
   an explicit clear was added, and even then swung 0↔17% run-to-run). Abandoned as
   default — the lesson that drove the geo node.
3. **`SdlDisplayListRenderNode`** (geo, **DEFAULT**) — a capture pass records a leaf's
   **layer-local tessellated geometry + text runs + icon glyphs BY PARAMS** (identity
   base CTM, no render target); `drawInto` re-emits through the layer transform. Crisp
   under any transform (geometry re-transformed, not resampled), bit-exact, **no
   render-target state → deterministic**. Text/icon capture is **eviction-safe**: it
   records run params (family/text/size/…), not texture pointers, and replay re-looks-up
   via the per-run LRU / FreeType glyph cache (re-rasterises if evicted). Ordered command
   stream (`GeometryBatch | TextRun | IconRun`) preserves z-order. **cache-leaves-defer-
   parents:** a child `drawInto` during a parent's record flags the parent to defer
   (draws nothing into the target-less capture pass) — nesting by defer, no by-value
   baking.

**Capture coverage:** geometry, plain text, spanned text (except `SpanStyle.background`),
icon-font glyphs — i.e. **every expensive (tessellate / shape / rasterise) op.**
Deferrals, all correct via block-replay: images (`drawImageRect`/`drawNativePainter`),
span backgrounds, `alpha<1`/blend/colorFilter/renderEffect layers, rounded/generic layer
clips, `saveLayer`, and parents with child layers.

**Perf (default now):** LazyColumn steady-state `draw` **3.55 → 1.52 ms (−57%)**, Tabs
−38%, NavRails −23%. **Never slower than Deferred** (a deferred leaf runs the identical
block-replay). Win ∝ how much plain geometry + plain text a screen has. Transform/alpha
animations are the best case — they change a node *property*, not content, so the display
list replays under the new transform without re-recording (same reason skiko replays a
RenderNode Picture under an updated matrix). **Granularity = the layer:** many-small-leaf
screens (LazyColumn, Tabs, list rows) cache big; monolithic single-layer screens re-record
wholesale — exactly upstream's RenderNode boundary.

## A.4 Hard-won learnings (durable — do not relearn)

- **Minimize-divergence is load-bearing.** A hand-written `notifyLayerIsDirty` that
  diverged from upstream `OwnedLayerManagerImpl` removed a layer from `dirtyLayers`
  mid-loop → `IndexOutOfBounds` crash on *any* navigation (screenshots missed it; the
  **probe** caught it). Fixed by matching upstream verbatim (clear-is-no-op while
  drawing; `clear()` after the loop). Don't hand-roll engine plumbing from a summary.
- **Offscreen-texture caching is timing-nondeterministic on complex screens** — the
  reason the geo (no-render-target) node is the robust path.
- **Rounded/path LAYER clips must be applied or deferred on the fast path.** The fast
  path submits raw geometry via `SDL_RenderGeometry`, which clips only to a RECT; the
  rounded clip is a lazy offscreen mask `replayBatch` bypasses. Symptom: the Carousel
  item's colour fill overflowed its morphing rounded mask (deterministic 4.4%). Fix: rect
  clip on the fast path (SDL clip rect honours it); rounded/generic → block-replay.
- **Pickers is NOT a render bug — it's screenshot timing.** A slow layout settle; geo's
  faster frames reach the fixed `--frames` capture point at a different settle phase.
  `geo-vs-geo` is 0.000% (deterministic) and phase-aligned `geo-vs-default` is 0.000%.
  Free-running screenshots on animated/settling screens give false signals → use a
  frame-locked/deterministic mode.
- **Rotated-edge AA fringe** is the one real static delta: geo rotates pre-tessellated
  local-space fringe vertices; block-replay tessellates in device space → <0.06%
  deterministic cosmetic diff on rotated shapes only (axis-aligned content is pixel-equal).
- **Verification layering:** `geo-vs-default` (same native stack) proves *self-
  consistency*, NOT upstream fidelity; only `parity.py` (native-vs-JVM) measures "match
  upstream." Screenshots miss crashes (use the probe) and settle-timing (use frame-lock).
- **Profiler caveat:** `present` is vsync-capped by the *display* refresh — profile on the
  target monitor before concluding a frame-rate gap (the demo-70/apidemo-144 report was
  two monitors, not a renderer bug).

## A.5 Carried-over future items (still valid)

- **Native-resource lifecycle** — wire `GraphicsLayer`/RenderNode + `SdlImageBitmap.
  close()` into cache eviction + the renderer `destroy()` chain; bounded `SkiaImageCache`
  (LRU + close-on-evict); demote the 10 s GC nudge once ownership covers it.
- **Glyph atlas** (→ Track B4 in the main plan) — SDL caches one texture per run-string;
  Skia uses a shared atlas (less memory, fewer binds, better batching). Less urgent now
  static text is recorded once and replayed.
- **SDL_GPU backend** (long-term) — real stencil clipping, pipelined batching, shader
  gradients. The `NativeRenderNode` seam makes a GPU node a clean drop-in later.
- **Feature-parity audit vs skiko** is the Phase-3 acceptance gate: per feature (save/
  saveLayer, transforms, clipRect/clipPath, draw primitives, Brush/gradients+TileMode,
  stroke/PathEffect, BlendMode, ColorFilter, RenderEffect, GraphicsLayer transforms,
  compositing/offscreen, layer alpha/clip/shadow) classify Matches / Gap / vendor-instead /
  out-of-scope (text shaping + GL/surface glue are out-of-scope by design). Skia-leg rows
  verify on mac/CI, SDL-leg rows on Windows.

## A.6 Explicit non-goals (recorded so they aren't re-attempted)

- **Dirty-region / partial-present** — upstream replays the whole scene; the win is
  not-re-recording, not redrawing-less.
- **A custom `cacheKey` API** — superseded by the real per-node display list.
- **Vendoring `RootNodeOwner` / the `ComposeScene` stack** — coupled to skiko's
  `SkiaLayer`/windowing; fights our `:window` SDL loop. We borrow only the *layer* engine.
