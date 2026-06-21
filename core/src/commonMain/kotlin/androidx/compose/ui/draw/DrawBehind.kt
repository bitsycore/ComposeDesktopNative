package androidx.compose.ui.draw

import androidx.compose.ui.DrawBehindModifier
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope

// ==================
// MARK: drawBehind
// ==================

/* Paint custom content under the modified node's children via a DrawScope
   block. Runs after the node's background / border draw step and before
   any children are painted. */
fun Modifier.drawBehind(onDraw: DrawScope.() -> Unit): Modifier =
	then(DrawBehindModifier(onDraw))
