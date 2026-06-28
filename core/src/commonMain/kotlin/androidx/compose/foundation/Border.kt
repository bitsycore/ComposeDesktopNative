package androidx.compose.foundation

import com.compose.desktop.native.element.BorderModifier
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp

// ==================
// MARK: Modifier.border()
// ==================

// BorderStroke is vendored verbatim from upstream and lives in
// core/src/vendor/.../foundation/BorderStroke.kt. It wraps a Brush; here we
// only act on the SolidColor case for now (the renderers' border paths read a
// flat Color), and silently no-op gradient brushes — match-the-signature
// without yet matching the behaviour.

/** Border with [width], solid [color], clipped to [shape]. */
fun Modifier.border(width: Dp, color: Color, shape: Shape = RectangleShape): Modifier =
    border(width, SolidColor(color), shape)

/**
 * Border with [width], [brush], clipped to [shape]. Public surface matches
 * upstream Border.kt's `brush:` overload. Only [SolidColor] brushes
 * actually paint today; gradient brushes silently no-op.
 */
fun Modifier.border(width: Dp, brush: Brush, shape: Shape = RectangleShape): Modifier {
    val vColor = (brush as? SolidColor)?.value ?: return this
    return then(BorderModifier(width.value.toInt(), vColor, shape))
}

/** Border specified by a [BorderStroke] (Brush + width), clipped to [shape]. */
fun Modifier.border(border: BorderStroke, shape: Shape = RectangleShape): Modifier =
    border(width = border.width, brush = border.brush, shape = shape)
