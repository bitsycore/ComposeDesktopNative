@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
internal fun DialogsScreen() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ScreenTitle(
            "Dialogs + overlays",
            "material3 AlertDialog, DropdownMenu, Snackbar, and TooltipBox — all on the popup host.",
        )

        // AlertDialog
        Section("AlertDialog", "Modal: confirm / dismiss buttons, click the scrim to dismiss.") {
            var vShow by remember { mutableStateOf(false) }
            Button(onClick = { vShow = true }) {
                Text("Open dialog", color = MaterialTheme.colorScheme.onPrimary)
            }
            if (vShow) {
                // Positional (not-named) onDismissRequest / confirmButton dodge an
                // overload-resolution ambiguity between the current and the deprecated
                // experimental AlertDialog signatures.
                AlertDialog(
                    { vShow = false },
                    {
                        Button(onClick = { vShow = false }) {
                            Text("Confirm", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    },
                    modifier = Modifier,
                    dismissButton = {
                        TextButton(onClick = { vShow = false }) {
                            Text("Cancel", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    title = { Text("Confirm action", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp) },
                    text = {
                        Text(
                            "Are you sure you want to proceed? This is a non-destructive demonstration.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                        )
                    },
                )
            }
        }

        // DropdownMenu
        Section("DropdownMenu", "Anchored popup with selectable items — anchors below the trigger. Click outside to dismiss.") {
            var vExpanded by remember { mutableStateOf(false) }
            var vSelected by remember { mutableStateOf("None") }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // The menu shares this Box with its trigger, so it anchors to the button.
                Box {
                    OutlinedButton(onClick = { vExpanded = !vExpanded }) {
                        Text("Open menu", color = MaterialTheme.colorScheme.primary)
                    }
                    DropdownMenu(expanded = vExpanded, onDismissRequest = { vExpanded = false }) {
                        for (vLabel in listOf("Apple", "Banana", "Cherry", "Date")) {
                            DropdownMenuItem(
                                text = { Text(vLabel, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp) },
                                onClick = { vSelected = vLabel; vExpanded = false },
                            )
                        }
                    }
                }
                Text("Selected: $vSelected", fontSize = 14.sp)
            }
        }

        // Snackbar
        Section("Snackbar", "Auto-dismissed transient toast. Pinned to bottom-center of the window.") {
            val vHost = remember { SnackbarHostState() }
            val vScope = rememberCoroutineScope()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vScope.launch { vHost.showSnackbar("Saved — your changes are live.") } }) {
                    Text("Show simple", color = MaterialTheme.colorScheme.onPrimary)
                }
                Button(onClick = {
                    vScope.launch { vHost.showSnackbar("Couldn't load profile.", actionLabel = "Retry") }
                }) { Text("With action", color = MaterialTheme.colorScheme.onPrimary) }
            }
            SnackbarHost(hostState = vHost)
        }

        // Tooltip
        Section("TooltipBox / PlainTooltip", "Hover the target — the tooltip appears anchored above it.") {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text("Save the document") } },
                    state = rememberTooltipState(),
                ) {
                    Button(onClick = {}) { Text("Save", color = MaterialTheme.colorScheme.onPrimary) }
                }
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text("Discard changes (cannot be undone).") } },
                    state = rememberTooltipState(),
                ) {
                    OutlinedButton(onClick = {}) { Text("Discard", color = MaterialTheme.colorScheme.primary) }
                }
            }
        }
    }
}
