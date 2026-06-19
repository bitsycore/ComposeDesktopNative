package androidx.compose.material

import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

// ==================
// MARK: Surface
// ==================

@Composable
fun Surface(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.surface,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.background(color),
        content = content
    )
}
