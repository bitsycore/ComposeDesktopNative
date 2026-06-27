package androidx.compose.foundation

import com.compose.desktop.native.element.FocusableModifier
import androidx.compose.ui.Modifier

// ==================
// MARK: focusable
// ==================

/* Marks this node as a focus target. The most recently clicked focusable
   ancestor becomes the focused node; clicks elsewhere remove focus. */
fun Modifier.focusable(onFocusChanged: (Boolean) -> Unit = {}) =
    then(FocusableModifier(onFocusChanged))
