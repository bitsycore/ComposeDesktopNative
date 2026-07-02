package androidx.compose.ui.graphics.vector

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter

// ==================
// MARK: ImageVector + rememberVectorPainter — stubs
// ==================

/*
 Phase 9 stubs — upstream ImageVector.kt is 704L declarative-vector graph +
 VectorPainter.kt walks a VectorNode tree at draw time. Vendored upstream
 `Image.kt` has an `Image(imageVector: ImageVector, ...)` overload that calls
 `rememberVectorPainter(vector)` and delegates to `Image(painter, ...)`.

 Keep marker types so Image.kt compiles; no runtime path exercises them yet
 (project material-symbols routes icon-font glyphs through the text pipeline,
 not through vector composition).
*/
class ImageVector

@Composable
fun rememberVectorPainter(@Suppress("UNUSED_PARAMETER") image: ImageVector): Painter =
	object : Painter() {
		override val intrinsicSize: Size get() = Size.Unspecified
		override fun DrawScope.onDraw() { /* no-op — needs ImageVector engine */ }
	}
