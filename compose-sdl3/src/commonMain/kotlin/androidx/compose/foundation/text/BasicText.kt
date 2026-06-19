package androidx.compose.foundation.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.node.LayoutNode
import androidx.compose.ui.node.MeasurePolicy
import androidx.compose.ui.node.NodeApplier
import androidx.compose.ui.unit.IntSize

// ==================
// MARK: BasicText
// ==================

@Composable
fun BasicText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: Int = 16
) {
    ComposeNode<LayoutNode, NodeApplier>(
        factory = { LayoutNode() },
        update = {
            set(text) { this.text = it }
            set(color) { this.textColor = it }
            set(fontSize) { this.fontSize = it }
            set(modifier) { this.modifier = it }
            set(Unit) {
                this.measurePolicy = TextMeasurePolicy
            }
        }
    )
}

/** Approximate text measurement (0.6 * fontSize per char width). */
internal val TextMeasurePolicy = MeasurePolicy { node, constraints ->
    val t = node.text ?: ""
    val fs = node.fontSize
    val charW = (fs * 0.6f).toInt().coerceAtLeast(1)
    val estW = charW * t.length
    val estH = (fs * 1.3f).toInt()

    val w = estW.coerceIn(constraints.minWidth, constraints.maxWidth)
    val h = estH.coerceIn(constraints.minHeight, constraints.maxHeight)
    IntSize(w, h)
}
