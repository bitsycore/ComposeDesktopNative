package apidemo

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

// The JVM comparison app's entry point. Runs the SAME shared App() as the
// native :apidemo, but on Compose Desktop (JVM) against upstream
// org.jetbrains.compose. Client-certificate (mTLS) features are native-only
// (they drive the bundled libcurl); the jvm actuals report that instead.
fun main() = application {
    Window(
        // The shared App installs a persist-then-close hook (InstallWindowHooks).
        onCloseRequest = { if (jvmOnCloseRequest?.invoke() != false) exitApplication() },
        onPreviewKeyEvent = { jvmOnKeyShortcut?.invoke(it) ?: false },
        title = "API Manager — JVM (upstream Compose)",
        state = rememberWindowState(width = 1240.dp, height = 820.dp),
    ) {
        App()
    }
}
