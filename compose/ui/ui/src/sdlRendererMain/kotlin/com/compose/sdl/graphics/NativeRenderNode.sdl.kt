package com.compose.sdl.graphics

// ==================
// MARK: createNativeRenderNode — SDL3 renderer actual (Phase 0 stub)
// ==================

/* Phase 0 scaffold: the seam exists and resolves, but nothing constructs a
   render node yet. The real SdlRenderNode (command-list display list + offscreen
   texture for requiresLayer() layers) lands in Phase 2/4 — see
   RENDERER_REFACTOR.md §4b. */
internal actual fun createNativeRenderNode(context: NativeRenderNodeContext): NativeRenderNode =
	TODO("SdlRenderNode not implemented yet — RENDERER_REFACTOR.md Phase 2 (display list + offscreen)")
