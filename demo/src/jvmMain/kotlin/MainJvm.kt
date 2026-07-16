import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import demo.registry.allCategories
import demo.shell.App
import org.jetbrains.skia.EncodedImageFormat
import java.io.File

// The JVM comparison app: the SAME shared App() + screens as :demo, on upstream
// Compose Desktop. Interactive by default; a headless screenshot mode drives
// the parity harness (scripts/parity — compares each screen native vs jvm).
//
//   --screenshot-all=<dir>   render every registered screen to <dir>/<Name>.png
//   --width / --height       viewport size (default 1000 / 700)
//
// The single-screen wrapper MIRRORS MainNative's --screen path (dark theme,
// verticalScroll + 24dp padding) so layout constraints match the native
// screenshots pixel-for-pixel.
fun main(args: Array<String>) {
    val screenshotDir = args.firstOrNull { it.startsWith("--screenshot-all=") }?.substringAfter('=')
    if (screenshotDir != null) {
        screenshotAllScreens(
            outDir = File(screenshotDir),
            width = args.intArg("--width", 1000),
            height = args.intArg("--height", 700),
        )
        return
    }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "ComposeDesktopNative — JVM (upstream Compose)",
            state = rememberWindowState(width = 1000.dp, height = 700.dp),
        ) {
            MaterialTheme(colorScheme = darkColorScheme()) {
                App(isJvm = true)
            }
        }
    }
}

private fun Array<String>.intArg(name: String, default: Int): Int =
    firstOrNull { it.startsWith("$name=") }?.substringAfter('=')?.toIntOrNull() ?: default

/* Render each registered screen headlessly (density 1 to match the native
   physical-pixel screenshots) and write a PNG per screen. */
@OptIn(ExperimentalComposeUiApi::class)
private fun screenshotAllScreens(outDir: File, width: Int, height: Int) {
    outDir.mkdirs()
    val screens = allCategories().flatMap { it.screens }.distinctBy { it.name }
    for (screen in screens) {
        val scene = ImageComposeScene(width, height, density = Density(1f)) {
            ScreenHost { screen.content() }
        }
        try {
            val image = scene.render()
            val png = image.encodeToData(EncodedImageFormat.PNG) ?: continue
            File(outDir, "${screen.name}.png").writeBytes(png.bytes)
        } finally {
            scene.close()
        }
        println("jvm screenshot: ${screen.name}")
    }
}

// P0.3 (RENDERER_CONVERGE.md §8): the SAME default font the native leg bundles
// (font/NotoSans.ttf, staged onto the JVM classpath by jvmProcessResources). Loading it
// into the parity JVM leg collapses the font-drift baseline to (near) just rasterizer AA —
// so the parity % measures real divergence, not "different default typeface". Null (→ no
// alignment, harness still runs) if the resource is missing.
private val notoSans: FontFamily? by lazy {
    val bytes = object {}.javaClass.getResourceAsStream("/font/NotoSans.ttf")?.readBytes()
    if (bytes == null) {
        println("parity(jvm): /font/NotoSans.ttf not on classpath — font NOT aligned")
        null
    } else FontFamily(Font(identity = "NotoSans", data = bytes))
}

/* A copy of the M3 Typography with every core style forced to [family] (Typography styles
   carry their own fontFamily, so overriding LocalTextStyle alone wouldn't reach them). */
private fun notoTypography(family: FontFamily): Typography {
    val b = Typography()
    return b.copy(
        displayLarge = b.displayLarge.copy(fontFamily = family),
        displayMedium = b.displayMedium.copy(fontFamily = family),
        displaySmall = b.displaySmall.copy(fontFamily = family),
        headlineLarge = b.headlineLarge.copy(fontFamily = family),
        headlineMedium = b.headlineMedium.copy(fontFamily = family),
        headlineSmall = b.headlineSmall.copy(fontFamily = family),
        titleLarge = b.titleLarge.copy(fontFamily = family),
        titleMedium = b.titleMedium.copy(fontFamily = family),
        titleSmall = b.titleSmall.copy(fontFamily = family),
        bodyLarge = b.bodyLarge.copy(fontFamily = family),
        bodyMedium = b.bodyMedium.copy(fontFamily = family),
        bodySmall = b.bodySmall.copy(fontFamily = family),
        labelLarge = b.labelLarge.copy(fontFamily = family),
        labelMedium = b.labelMedium.copy(fontFamily = family),
        labelSmall = b.labelSmall.copy(fontFamily = family),
    )
}

/* Same wrapper as MainNative's --screen path — plus NotoSans font-alignment (P0.3) so the
   parity diff isn't dominated by the JVM's default typeface. */
@Composable
private fun ScreenHost(content: @Composable () -> Unit) {
    val family = notoSans
    MaterialTheme(
        colorScheme = darkColorScheme(),
        typography = if (family != null) notoTypography(family) else Typography(),
    ) {
        val base = LocalTextStyle.current
        CompositionLocalProvider(
            LocalTextStyle provides (if (family != null) base.copy(fontFamily = family) else base),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            ) { content() }
        }
    }
}
