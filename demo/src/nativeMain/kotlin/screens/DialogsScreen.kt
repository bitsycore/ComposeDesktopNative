@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package screens
import demo.DropdownMenu
import demo.DropdownMenuItem
import demo.TooltipBox
import demo.menuAnchor
import demo.rememberMenuAnchor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun DialogsScreen() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ScreenTitle(
            "Dialogs + overlays",
            "Built on the popup host installed by composeWindow — appear above all content.",
        )

        // Dialog
        Section("Dialog / AlertDialog", "Modal: click the scrim to dismiss.") {
            var vShow by remember { mutableStateOf(false) }
            Button(onClick = { vShow = true }) {
                Text("Open dialog", color = MaterialTheme.colorScheme.onPrimary)
            }
            if (vShow) {
                // Full m3 AlertDialog (title/text/confirmButton/dismissButton slots).
                // The `expect fun AlertDialog(onDismissRequest, confirmButton, ..., title, text, ...)`
                // isn't marked ExperimentalMaterial3Api itself, but overload resolution
                // seems to still route through the deprecated experimental variant
                // (`fun AlertDialog(onDismissRequest, modifier, properties, content)` at
                // line 213 of material3/AlertDialog.kt) even with @OptIn — probably a
                // Kotlin frontend issue with @Deprecated + @ExperimentalMaterial3Api overloads.
                // Positional (not-named) arguments dodge the ambiguity.
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
        Section("DropdownMenu", "Anchored popup with selectable items. Click outside to dismiss. The anchor's window-coordinate position is tracked via Modifier.menuAnchor.") {
            var vExpanded by remember { mutableStateOf(false) }
            val vAnchor = rememberMenuAnchor()
            var vSelected by remember { mutableStateOf("None") }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { vExpanded = !vExpanded },
                    modifier = Modifier.menuAnchor(vAnchor),
                ) { Text("Open menu", color = MaterialTheme.colorScheme.primary) }
                DropdownMenu(
                    expanded = vExpanded,
                    onDismissRequest = { vExpanded = false },
                    anchor = vAnchor,
                    offsetY = 4.dp,
                ) {
                    for (vLabel in listOf("Apple", "Banana", "Cherry", "Date")) {
                        DropdownMenuItem(onClick = { vSelected = vLabel; vExpanded = false }) {
                            Text(vLabel, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
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
        Section("TooltipBox", "Hover the target — text appears below it after a 600 ms delay.") {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                TooltipBox(text = "Save the document") {
                    Button(onClick = {}) { Text("Save", color = MaterialTheme.colorScheme.onPrimary) }
                }
                TooltipBox(text = "Discard changes (cannot be undone).") {
                    OutlinedButton(onClick = {}) { Text("Discard", color = MaterialTheme.colorScheme.primary) }
                }
            }
        }
    }
}
