package androidx.compose.ui.graphics

import androidx.compose.ui.graphics.drawscope.DrawScope

// ==================
// MARK: MeshGradientRenderer — native no-op actual
// ==================

/*
 Actual for vendored MeshGradientRenderer.kt. Upstream's skikoMain uses a
 700+L software rasterizer to shade a mesh gradient into an ImageBitmap;
 we don't have that yet. Return a no-op renderer that draws nothing —
 mesh gradients aren't used by our project's demos.
*/
private object NoOpMeshGradientRenderer : MeshGradientRenderer {
	override fun DrawScope.draw(config: MeshGradientConfig) { /* no-op */ }
}

internal actual fun MeshGradientRenderer(): MeshGradientRenderer = NoOpMeshGradientRenderer
