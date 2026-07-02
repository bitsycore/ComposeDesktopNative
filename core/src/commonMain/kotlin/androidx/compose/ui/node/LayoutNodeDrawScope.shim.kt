package androidx.compose.ui.node

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.layer.GraphicsLayer

// Phase 9 stub — upstream LayoutNodeDrawScope drives NodeCoordinator's draw pass
// (delegating to CanvasDrawScope). Our renderers draw the tree directly, bypassing
// this path, so a no-op `draw(...)` matching NodeCoordinator's call is enough.
internal class LayoutNodeDrawScope {
	fun draw(canvas: Canvas, size: Size, coordinator: NodeCoordinator, drawNode: Modifier.Node, layer: GraphicsLayer?) {}
}
