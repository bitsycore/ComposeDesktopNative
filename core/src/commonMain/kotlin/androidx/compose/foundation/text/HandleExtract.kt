package androidx.compose.foundation.text

// ==================
// MARK: Handle / HandleState — extract
// ==================

/*
 Extracted from upstream `foundation.text.CoreTextField.kt` (1000+L massive
 file — the whole legacy TextField pipeline we're not vendoring yet).
 SelectionManager + TextFieldSelectionManager both reference these types
 for the mobile-style drag-handle affordance we don't render on desktop.
 `HandleHeight` (also referenced by SelectionManager) is separately
 extracted in `selection/HandleHeightExtract.kt`.

 TODO: delete this file once CoreTextField.kt can vendor cleanly
 (needs the whole LegacyTextFieldState pipeline).
*/

/** Handle style within a text field. */
internal enum class Handle {
	Cursor,
	SelectionStart,
	SelectionEnd,
}

/** State of the visible handle rendered next to text selection. */
internal enum class HandleState {
	None,
	Selection,
	Cursor,
}
