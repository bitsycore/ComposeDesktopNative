package com.compose.desktop.native

import com.compose.desktop.native.renderer.skia.SkiaBridge
import com.compose.desktop.native.renderer.skia.SkiaMetalBridge

// ==================
// MARK: macOS GPU defaults
// ==================
// preferredGpuMode + makeMetalBridge are actuals, so they stay in the core
// package (matching their expects) and construct the bridge from renderer.skia.

internal actual fun preferredGpuMode(): GpuMode = GpuMode.Skia.Metal

internal actual fun makeMetalBridge(backend: SDL3Backend): SkiaBridge? {
	val bridge = SkiaMetalBridge(backend)
	return if (bridge.init()) bridge else null
}
