package com.compose.sdl.graphics

import androidx.compose.ui.graphics.GraphicsContext

// ==================
// MARK: createGraphicsContext — Skia renderer (transient, pre-P1.6)
// ==================

/* TRANSIENT: returns the project GraphicsContext so the Skia leg keeps the port
   GraphicsLayer while behaviour is unchanged. P1.6 replaces this with the vendored
   upstream SkiaGraphicsContext (which owns a skiko RenderNodeContext) and deletes the
   skiko-side port cluster. See RENDERER_TASKS.md P1.3/P1.6 + RENDERER_CONVERGE.md §4. */
internal actual fun createGraphicsContext(): GraphicsContext = ProjectGraphicsContext()
