package androidx.compose.foundation.shape

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.min

// ==================
// MARK: CircleShape
// ==================

val CircleShape: Shape = object : Shape {
	override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
		val vR = min(size.width, size.height) / 2f
		return Outline.Rounded(RoundRect(rect = Rect(0f, 0f, size.width, size.height), cornerRadius = CornerRadius(vR, vR)))
	}
	override fun toString(): String = "CircleShape"
}

// ==================
// MARK: RoundedCornerShape
// ==================

/* Uniform-corner rounded rectangle. NOTE: official RoundedCornerShape is a
   CornerBasedShape with per-corner CornerSize; this is a reduced
   uniform-radius impl (see CLAUDE.md). */
class RoundedCornerShape(private val radius: Dp) : Shape {
	override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
		val vR = with(density) { radius.toPx() }
		return if (vR <= 0f) Outline.Rectangle(Rect(0f, 0f, size.width, size.height))
		else Outline.Rounded(RoundRect(rect = Rect(0f, 0f, size.width, size.height), cornerRadius = CornerRadius(vR, vR)))
	}

	override fun equals(other: Any?): Boolean =
		other is RoundedCornerShape && other.radius == radius
	override fun hashCode(): Int = radius.hashCode()
	override fun toString(): String = "RoundedCornerShape(radius=$radius)"
}

/* Convenience matching Compose's RoundedCornerShape(Int) — percent of the
   shorter side (50 = pill / circle). */
fun RoundedCornerShape(percent: Int): Shape = object : Shape {
	override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
		val vR = min(size.width, size.height) * percent.coerceIn(0, 50) / 100f
		return if (vR <= 0f) Outline.Rectangle(Rect(0f, 0f, size.width, size.height))
		else Outline.Rounded(RoundRect(rect = Rect(0f, 0f, size.width, size.height), cornerRadius = CornerRadius(vR, vR)))
	}
}
