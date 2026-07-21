package apidemo.compat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.platform.ClipEntry
import com.compose.sdl.LocalComposeNativeWindow
import com.compose.sdl.TextLayoutConfig
import com.compose.sdl.appDataDir
import com.compose.sdl.fileManagerName
import com.compose.sdl.registerMemoryResource
import com.compose.sdl.removeMemoryResource
import com.compose.sdl.res.ResourceKind
import com.compose.sdl.res.painterResource
import com.compose.sdl.revealInFileManager
import com.compose.sdl.showOpenFileDialog
import com.compose.sdl.showSaveFileDialog
import com.compose.sdl.text.currentTextMeasurer
import io.ktor.client.*
import io.ktor.client.engine.curl.*
import okio.FileSystem

// ==================
// MARK: Native actuals — pure delegation to the port's SDL-backed APIs
// ==================

actual fun appDataDir(inOrg: String, inApp: String): String? =
    appDataDir(inOrg, inApp)

actual fun revealInFileManager(inPath: String, inOnResult: ((Boolean) -> Unit)?) =
    revealInFileManager(inPath, inOnResult)

actual fun fileManagerName(): String = fileManagerName()

actual fun showSaveFileDialog(inDefaultName: String?, inOnResult: (String?) -> Unit) =
    showSaveFileDialog(inDefaultName, inOnResult)

actual fun showOpenFileDialog(inOnResult: (String?) -> Unit) =
    showOpenFileDialog(inOnResult)

actual fun clipEntryOfText(inText: String): ClipEntry = ClipEntry.withPlainText(inText)

actual fun registerMemoryImage(inKey: String, inBytes: ByteArray) = registerMemoryResource(inKey, inBytes)

actual fun removeMemoryImage(inKey: String) = removeMemoryResource(inKey)

@Composable
actual fun memoryImagePainter(inKey: String, inSvg: Boolean): Painter =
    painterResource(inKey, if (inSvg) ResourceKind.Svg else ResourceKind.Raster)

actual fun wrappedRowCount(inText: String, inFontPx: Int, inMaxWidthPx: Int, inFamilyName: String?): Int =
    currentTextMeasurer.wrap(inText, inFontPx, inMaxWidthPx, inFamilyName).lines.size

actual var editorTabWidth: Int
    get() = TextLayoutConfig.tabWidth
    set(value) {
        TextLayoutConfig.tabWidth = value
    }

internal actual val systemFileSystem: FileSystem = FileSystem.SYSTEM

actual fun createApiHttpClient(): HttpClient =
    HttpClient(Curl)

@Composable
actual fun InstallWindowHooks(inOnCloseRequest: () -> Boolean, inOnKeyShortcut: (KeyEvent) -> Boolean) {
    val vWindow = LocalComposeNativeWindow.current
    DisposableEffect(inOnCloseRequest, inOnKeyShortcut) {
        vWindow.setOnCloseRequest(inOnCloseRequest)
        vWindow.setOnKeyShortcut(inOnKeyShortcut)
        onDispose { vWindow.setOnCloseRequest(null); vWindow.setOnKeyShortcut(null) }
    }
}
