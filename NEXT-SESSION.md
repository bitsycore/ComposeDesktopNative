# Next session — renderer caching / retained layers

Starting point after v0.1.19. Context for the perf work the profiling pointed at.

## The problem (measured)

The demo caps at ~70 fps on every screen while apidemo hits 144. Cause is NOT
vsync (both use `SDL_SetRenderVSync(renderer, 1)`) — the demo is **draw-bound**:
`CDN_PROFILE=1` showed draw ~34 ms cold / ~14 ms steady, present only ~1.3 ms.
The renderer is **immediate-mode**: every rendered frame walks the layout tree
and re-tessellates everything (text → glyph quads, shapes → triangles). The
always-present sidebar (30+ text rows + icons) is re-tessellated each frame.

Two open threads:

### A. Why does the demo render CONTINUOUSLY? (investigate FIRST — likely cheaper)

Headless the demo idled (2 frames); interactively you see a constant 70-71 fps,
so something invalidates every frame. Prime suspect: a **hover self-loop** — the
main loop dispatches a synthetic hover every rendered frame
(`ComposeWindow.renderFrame`: `if (hasMousePos) host.onPointerRaw(...)`), and if
that perpetually re-invalidates a hover-reactive sidebar row, the app never
idles while the cursor is over it. If confirmed, the fix is to stop the spurious
invalidation so static screens idle (→ sidebar stops re-tessellating because
nothing renders) — small and targeted, no caching needed.
- Quick test: does the demo's FPS-title stop updating when the mouse leaves the
  window? If yes → hover loop confirmed.
- Look at: `renderFrame` synthetic hover; how hover state feeds `needsFrame` /
  `hasPendingWork` / `shouldRender()`.

### B. cacheKey / retained-layer texture caching (bigger — the "renderer rewrite")

`Modifier.graphicsLayer(cacheKey=…)` exists as API (`GraphicsLayerModifier` /
`GraphicsLayerNode` in `element/ModifierElements.kt`) but is **dead
scaffolding**: `cacheKey` is stored and never read by the renderer; the node is
a bare `Modifier.Node` with no draw behaviour (comment: "stays dormant until the
renderer rewrite drives it"). The demo's GraphicsLayer "(cached)" section uses a
plain `graphicsLayer()` — the label is aspirational; nothing is cached.

Blocker for real texture caching: it needs to render a subtree's `drawContent()`
into an **offscreen canvas**, and the renderer has no way to redirect
`drawContent()` off the frame canvas. `GraphicsLayer.native.draw()` replays its
recorded block against a canvas via `drawScope.draw(…, canvas, …)`, but
`drawContent()` is bound to the outer `ContentDrawScope`'s canvas, which nothing
swaps. The vector/icon offscreen path works only because it draws an explicit
object, not opaque `drawContent()`.

So the real unit of work is a **content-redirect primitive**: make the
`ContentDrawScope` canvas swappable (or add a coordinator-level "draw this
subtree into canvas X"). That primitive is the foundation for BOTH cacheKey
(retained layers) AND dirty-region rendering. Infrastructure that already
exists to build on: `OffscreenRenderer` (`createImageBitmap` + `createCanvas`),
`SdlImageBitmap` render-target textures + `drawImage` blit-back, and the
`NativeReleaseQueue` for freeing cached textures.

Cleanup to do regardless: either implement `cacheKey` or delete the dead
`GraphicsLayerModifier`/`GraphicsLayerNode` scaffolding and fix the demo's
misleading "(cached)" label.

## Tooling ready for this work

- `CDN_PROFILE=1 <app>` → per-phase timings (layout/draw/present) to a file.
- `scripts/parity/parity.py` → native-vs-JVM screenshot diff (regression net).
- `scripts/probe/probe.py` → drive a native window (click/hover/hold) + capture.
- See ROADMAP.md item 2 (dirty regions + retained layers promoted, with the
  demo evidence) and CLAUDE.md "Tooling".
