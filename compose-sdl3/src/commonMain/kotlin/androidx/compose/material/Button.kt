package androidx.compose.material

import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ==================
// MARK: Button
// ==================

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    content: @Composable () -> Unit
) {
    val bgColor = if (enabled) colors.backgroundColor else Color.Gray
    Box(
        modifier = modifier
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { if (enabled) onClick() },
        contentAlignment = Alignment.Center,
        content = content
    )
}

// ==================
// MARK: ButtonDefaults
// ==================

object ButtonDefaults {
    @Composable
    fun buttonColors(
        backgroundColor: Color = MaterialTheme.colors.primary,
        contentColor: Color = MaterialTheme.colors.onPrimary,
    ) = ButtonColors(backgroundColor, contentColor)
}

data class ButtonColors(
    val backgroundColor: Color,
    val contentColor: Color
)
