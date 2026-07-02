package androidx.compose.ui.graphics.painter

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope

// ==================
// MARK: BitmapPainter / ColorPainter stubs
// ==================

/*
 Phase 9 stubs — upstream BitmapPainter needs `ImageBitmap` (expect class with
 native actual) and ColorPainter is a tiny concrete Painter subclass. Vendored
 upstream `Image.kt` and `PainterModifier.kt` reference these types by name;
 keep minimal declarations so those files compile without pulling the whole
 ImageBitmap engine.

 - `BitmapPainter` — accepts an `Any` (upstream's ImageBitmap); onDraw is a
   no-op. Runtime code paths that reach Image(bitmap) will paint nothing until
   a real ImageBitmap engine + BitmapPainter land.
 - `ColorPainter` — real concrete Painter subclass (only 63 upstream lines);
   paints a solid Color. `drawRect(color)` in our DrawScope works today.
*/
class BitmapPainter(
	@Suppress("unused") val image: Any,
	@Suppress("unused") val srcOffset: androidx.compose.ui.unit.IntOffset =
		androidx.compose.ui.unit.IntOffset.Zero,
	@Suppress("unused") val srcSize: androidx.compose.ui.unit.IntSize =
		androidx.compose.ui.unit.IntSize.Zero,
	@Suppress("unused") val filterQuality: androidx.compose.ui.graphics.FilterQuality =
		androidx.compose.ui.graphics.drawscope.DrawScope.DefaultFilterQuality,
) : Painter() {
	override val intrinsicSize: Size get() = Size.Unspecified
	override fun DrawScope.onDraw() { /* no-op — needs real ImageBitmap engine */ }
}

class ColorPainter(val color: Color) : Painter() {

	private var alpha: Float = 1f
	private var colorFilter: ColorFilter? = null

	override val intrinsicSize: Size get() = Size.Unspecified

	override fun DrawScope.onDraw() {
		drawRect(color = color, alpha = alpha, colorFilter = colorFilter)
	}

	override fun applyAlpha(alpha: Float): Boolean { this.alpha = alpha; return true }
	override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
		this.colorFilter = colorFilter; return true
	}

	override fun equals(other: Any?): Boolean = other is ColorPainter && other.color == color
	override fun hashCode(): Int = color.hashCode()
	override fun toString(): String = "ColorPainter(color=$color)"
}
