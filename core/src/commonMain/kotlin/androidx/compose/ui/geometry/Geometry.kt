package androidx.compose.ui.geometry

// ==================
// MARK: Offset / Size
// ==================

/* Pixel-space 2D point used by DrawScope primitives. Components are in
   logical points (the same coordinate space as LayoutNode geometry). */
data class Offset(val x: Float, val y: Float) {
	operator fun plus(inOther: Offset) = Offset(x + inOther.x, y + inOther.y)
	operator fun minus(inOther: Offset) = Offset(x - inOther.x, y - inOther.y)

	companion object {
		val Zero = Offset(0f, 0f)
	}
}

/* Pixel-space 2D extent — width × height in logical points. */
data class Size(val width: Float, val height: Float) {
	val minDimension: Float get() = if (width < height) width else height

	companion object {
		val Zero = Size(0f, 0f)
	}
}
