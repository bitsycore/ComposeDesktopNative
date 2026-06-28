package androidx.compose.ui.graphics

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.boundingRect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

// ==================
// MARK: Shape
// ==================

/* Defines an Outline geometry for a given (size, layout direction, density).
   Renderers walk the chain looking for Background/Border/Clip modifiers,
   ask the modifier's Shape for its Outline, and dispatch to the right
   fill / stroke / clip primitive. Matches official Compose's
   androidx.compose.ui.graphics.Shape. */
interface Shape {
	fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline
}

// ==================
// MARK: Outline
// ==================

/* Reduced reimplementation of androidx.compose.ui.graphics.Outline. Upstream
   provides Canvas/DrawScope `drawOutline(outline, paint/...)` extensions
   that depend on engine glue we don't host (`Canvas` is an `expect class`,
   `drawRoundRect(brush, topLeft, size, cornerRadius: CornerRadius)`).
   The public sealed-class shape matches upstream byte-for-byte; the
   renderers read `bounds`, `rect`, `roundRect`, and `path` directly and
   dispatch through their own paint primitives. */
sealed class Outline {
	abstract val bounds: Rect

	/* Rectangular area. */
	class Rectangle(val rect: Rect) : Outline() {
		override val bounds: Rect get() = rect
		override fun equals(other: Any?): Boolean =
			this === other || (other is Rectangle && other.rect == rect)
		override fun hashCode(): Int = rect.hashCode()
	}

	/* Rectangular area with rounded corners (may differ per corner). */
	class Rounded(val roundRect: RoundRect) : Outline() {
		override val bounds: Rect get() = roundRect.boundingRect
		override fun equals(other: Any?): Boolean =
			this === other || (other is Rounded && other.roundRect == roundRect)
		override fun hashCode(): Int = roundRect.hashCode()
	}

	/* Free-form path. */
	class Generic(val path: Path) : Outline() {
		// Path doesn't track its bounds; renderers that hit this branch already
		// know the node's layout rect, so we report Rect.Zero here and let the
		// renderer fall back to its own bounds.
		override val bounds: Rect get() = Rect.Zero
		// No equals/hashCode — two outlines built from the same Path shouldn't
		// be considered equal since the Path is mutable.
	}
}

// RectangleShape lives in the vendored core/src/vendor/.../RectangleShape.kt
// (byte-for-byte from upstream).
