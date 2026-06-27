package androidx.compose.ui.state

// ==================
// MARK: ToggleableState
// ==================

/* Tri-state toggle value, matching androidx.compose.ui.state.ToggleableState.
   Used by Checkbox / TriStateCheckbox. */
enum class ToggleableState {
    On,
    Off,
    Indeterminate,
}

/* Convenience factory: On for true, Off for false (official top-level fun). */
fun ToggleableState(value: Boolean): ToggleableState =
    if (value) ToggleableState.On else ToggleableState.Off
