package com.compose.sdl.graphics

// ==================
// MARK: createNativeRenderNode — Skia renderer actual (Phase 0 stub)
// ==================

/* Phase 0 scaffold: the seam exists and resolves, but nothing constructs a
   render node yet. Phase 1 backs this with skiko's org.jetbrains.skiko.node.RenderNode
   (option S1) or org.jetbrains.skia.Picture (option S2), wrapping upstream's
   SkiaGraphicsLayer record/replay almost verbatim — see RENDERER_REFACTOR.md §4a. */
internal actual fun createNativeRenderNode(context: NativeRenderNodeContext): NativeRenderNode =
	TODO("Skia RenderNode-backed node not implemented yet — RENDERER_REFACTOR.md Phase 1")
