package apidemo

import com.compose.sdl.nativeComposeWindow

// ==================
// MARK: Entry point (native — SDL window shell)
// ==================

fun main() {
    nativeComposeWindow(title = "API Manager", width = 1240, height = 820) { App() }
}
