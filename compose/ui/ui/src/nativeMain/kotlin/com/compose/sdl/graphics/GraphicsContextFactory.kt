package com.compose.sdl.graphics

import androidx.compose.ui.graphics.GraphicsContext
import androidx.compose.ui.graphics.layer.GraphicsLayer

// ==================
// MARK: GraphicsContext factory seam (B2 / P1.3)
// ==================

/* ComposeOwner.graphicsContext used to be an ad-hoc anonymous GraphicsContext.
   B2 needs the Skia leg to swap in upstream's own SkiaGraphicsContext (which owns a
   skiko RenderNodeContext), so the context is created behind a per-renderer factory
   seam: exactly one actual is attached per target (the createRenderBackend trick).
   SDL keeps [ProjectGraphicsContext]; the Skia actual returns it too until P1.6
   replaces that side with the vendored SkiaGraphicsContext. See RENDERER_CONVERGE.md
   §4 (B2). */
internal expect fun createGraphicsContext(): GraphicsContext

/**
 * The project GraphicsContext used by the SDL leg (and, transiently, the Skia leg
 * pre-P1.6). GraphicsLayer's `expect class` hides its constructor/release from common
 * code, so layer creation/release hop through the [createProjectGraphicsLayer] /
 * [releaseProjectGraphicsLayer] factories. `shadowContext` keeps the interface default.
 */
internal class ProjectGraphicsContext : GraphicsContext {
	override fun createGraphicsLayer(): GraphicsLayer = createProjectGraphicsLayer()
	override fun releaseGraphicsLayer(layer: GraphicsLayer) = releaseProjectGraphicsLayer(layer)
}
