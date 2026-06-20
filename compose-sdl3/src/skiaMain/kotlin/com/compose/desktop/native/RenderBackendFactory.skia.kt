package com.compose.desktop.native

import com.compose.desktop.native.renderer.skia.SkiaRenderBackend

// ==================
// MARK: makeRenderBackend (Skia targets)
// ==================

/* Skia renderer factory — the actual must live in the same package as the
   expect (core), so it stays here and constructs the SkiaRenderBackend that
   lives in renderer.skia. This source set has no SDL3 renderer / SDL3_ttf
   cinterop, so reject Sdl3.* with a clear message. */
internal actual fun makeRenderBackend(inSdl: SDL3Backend, inGpu: GpuMode): RenderBackend? {
	val vResolved = if (inGpu is GpuMode.Auto) preferredGpuMode() else inGpu
	if (vResolved is GpuMode.Sdl3) {
		error("Sdl3.* modes aren't available in a Skia build — rerun with -Prenderer=sdl3")
	}
	return try {
		SkiaRenderBackend(inSdl, vResolved)
	} catch (t: Throwable) {
		println("makeRenderBackend failed: ${t.message}")
		null
	}
}
