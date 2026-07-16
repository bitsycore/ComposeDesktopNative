package com.compose.sdl.graphics

// ==================
// MARK: createNativeRenderNode — Skia renderer (transient, pre-B2/P1.6)
// ==================

/* Returns the port DeferredRenderNode (replay-the-block). This whole node cluster
   (GraphicsLayer.native / NativeRenderNode / DeferredRenderNode) is a TRANSIENT copy
   on the Skia leg: P1.2 relocated the shared port cluster into each renderer source
   set so the Skia leg keeps the port GraphicsLayer while behaviour is unchanged; P1.6
   replaces this side with upstream's SkiaGraphicsLayer + skiko RenderNode and deletes
   this file. See RENDERER_TASKS.md P1.2/P1.6 + RENDERER_CONVERGE.md §4 (B2). */
internal fun createNativeRenderNode(context: NativeRenderNodeContext): NativeRenderNode =
	DeferredRenderNode()
