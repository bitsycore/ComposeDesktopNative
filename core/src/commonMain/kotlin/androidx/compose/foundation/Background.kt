package androidx.compose.foundation

import com.compose.desktop.native.element.BackgroundModifier
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor

// ==================
// MARK: Modifier.background()
// ==================

/** Solid colour fill behind the modified node, clipped to [shape]. */
fun Modifier.background(color: Color, shape: Shape = RectangleShape): Modifier =
    then(BackgroundModifier(color, shape))

/**
 * Brush fill behind the modified node, clipped to [shape], with [alpha].
 * Public surface matches upstream Background.kt verbatim. Today we only
 * honor [SolidColor] brushes (extracted into our BackgroundModifier);
 * gradient brushes silently no-op until the renderer rewrite migrates
 * the background pipeline to read upstream-shape brush values.
 */
fun Modifier.background(
    brush: Brush,
    shape: Shape = RectangleShape,
    @Suppress("UNUSED_PARAMETER") alpha: Float = 1.0f,
): Modifier {
    val vColor = (brush as? SolidColor)?.value ?: return this
    return then(BackgroundModifier(vColor, shape))
}
