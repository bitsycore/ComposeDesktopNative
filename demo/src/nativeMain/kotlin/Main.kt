import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import sdl3backend.composeWindow

fun main() {
    composeWindow(title = "ComposeNativeSDL3 Demo", width = 800, height = 600) {
        MaterialTheme(colors = darkColors()) {
            App()
        }
    }
}

// ==================
// MARK: Demo App
// ==================

@Composable
fun App() {
    var counter by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ComposeNativeSDL3",
                color = MaterialTheme.colors.primary,
                fontSize = 32
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Counter: $counter",
                fontSize = 24
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { counter-- }) {
                    Text(text = "  -  ", color = MaterialTheme.colors.onPrimary)
                }

                Button(onClick = { counter = 0 }) {
                    Text(text = " Reset ", color = MaterialTheme.colors.onPrimary)
                }

                Button(onClick = { counter++ }) {
                    Text(text = "  +  ", color = MaterialTheme.colors.onPrimary)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Click the buttons above!",
                color = Color.Gray,
                fontSize = 14
            )
        }
    }
}
