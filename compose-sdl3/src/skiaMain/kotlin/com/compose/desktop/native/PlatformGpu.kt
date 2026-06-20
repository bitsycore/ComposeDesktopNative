package com.compose.desktop.native

import com.compose.desktop.native.renderer.skia.SkiaBridge

// ==================
// MARK: Metal bridge factory (skia-only)
// ==================

/* Constructs the platform-specific Metal bridge, or null if Metal isn't
   supported on this target. Linux returns null; macOS returns the real
   thing. Only used by SkiaRenderBackend. */
internal expect fun makeMetalBridge(backend: SDL3Backend): SkiaBridge?
