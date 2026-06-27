package androidx.compose.foundation

import com.compose.desktop.native.element.BackgroundModifier
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape

// ==================
// MARK: Modifier.background()
// ==================

fun Modifier.background(color: Color, shape: Shape = RectangleShape) =
    then(BackgroundModifier(color, shape))
