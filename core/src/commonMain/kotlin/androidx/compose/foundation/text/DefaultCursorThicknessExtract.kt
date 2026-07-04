package androidx.compose.foundation.text

import androidx.compose.ui.unit.dp

// ==================
// MARK: DefaultCursorThickness — extract
// ==================

/*
 Extracted from upstream `foundation.text.TextFieldCursor.kt` (unvendored — it
 needs LegacyTextFieldState). Upstream declares this as an `expect val` and
 per-platform actuals; every platform picks 2.dp. TextFieldScroll (vendored)
 references it directly, so we provide the const.

 TODO: delete when TextFieldCursor.kt can vendor cleanly.
*/
internal val DefaultCursorThickness = 2.dp
