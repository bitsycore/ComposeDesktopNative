package androidx.compose.foundation

import androidx.compose.ui.BorderModifier
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

// ==================
// MARK: Modifier.border()
// ==================

fun Modifier.border(width: Dp, color: Color) = then(BorderModifier(width.value.toInt(), color))
