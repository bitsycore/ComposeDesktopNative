package androidx.compose.material

import androidx.compose.runtime.Composable
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

// ==================
// MARK: Text (Material)
// ==================

@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onBackground,
    fontSize: Int = 16
) {
    BasicText(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize
    )
}
