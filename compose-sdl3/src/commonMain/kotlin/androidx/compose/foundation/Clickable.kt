package androidx.compose.foundation

import androidx.compose.ui.ClickableModifier
import androidx.compose.ui.Modifier

// ==================
// MARK: Modifier.clickable()
// ==================

fun Modifier.clickable(onClick: () -> Unit) = then(ClickableModifier(onClick))
