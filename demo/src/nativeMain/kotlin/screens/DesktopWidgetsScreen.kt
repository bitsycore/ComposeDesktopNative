package screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.sdl.widgets.HorizontalSplitPane
import com.compose.sdl.widgets.VerticalSplitPane

@Composable
internal fun DesktopWidgetsScreen() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ScreenTitle(
            "Desktop widgets",
            "SplitPane resizable panes (project-original, com.compose.sdl.widgets), plus segmented & toggle buttons.",
        )

        // SplitPane
        Section("HorizontalSplitPane", "Drag the centre divider to resize the panes.") {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                HorizontalSplitPane(
                    initialFirstSize = 220.dp,
                    first = {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center,
                        ) { Text("Sidebar", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp) }
                    },
                    second = {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center,
                        ) { Text("Editor", color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp) }
                    },
                )
            }
        }

        Section("VerticalSplitPane", "Top/bottom variant.") {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                VerticalSplitPane(
                    initialFirstSize = 100.dp,
                    first = {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center,
                        ) { Text("Editor", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp) }
                    },
                    second = {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center,
                        ) { Text("Output", color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp) }
                    },
                )
            }
        }
    }
}
