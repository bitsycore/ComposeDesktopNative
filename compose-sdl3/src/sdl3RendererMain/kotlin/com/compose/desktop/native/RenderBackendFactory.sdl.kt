package com.compose.desktop.native

import com.compose.desktop.native.renderer.sdl.Sdl3RenderBackend

// ==================
// MARK: makeRenderBackend (mingwX64 / -Prenderer=sdl3)
// ==================

/* SDL3 renderer factory — the actual must live in the same package as the
   expect (core), so it stays here and constructs the Sdl3RenderBackend that
   lives in renderer.sdl. Linked on mingwX64 (always) and on macOS / Linux
   when -Prenderer=sdl3; the Skia bridges aren't on the classpath in those
   builds, so reject Skia.* upfront with a clear message. */
internal actual fun makeRenderBackend(inSdl: SDL3Backend, inGpu: GpuMode): RenderBackend? {
	val vResolved = if (inGpu is GpuMode.Auto) preferredGpuMode() else inGpu
	if (vResolved is GpuMode.Skia) {
		error("$vResolved isn't available in this build — Skiko isn't linked. " +
			"Rerun without -Prenderer=sdl3 (on macOS / Linux) to use Skia, " +
			"or pick a GpuMode.Sdl3.* / GpuMode.None.")
	}
	return try {
		Sdl3RenderBackend(inSdl)
	} catch (t: Throwable) {
		println("makeRenderBackend (sdl3) failed: ${t.message}")
		null
	}
}
