package androidx.compose.foundation

import androidx.compose.ui.BackgroundModifier
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

// ==================
// MARK: Modifier.background()
// ==================

fun Modifier.background(color: Color) = then(BackgroundModifier(color))
