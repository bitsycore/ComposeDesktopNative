package demo.shim

import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.skia.Image
import org.jetbrains.skiko.toComposeImageBitmap

actual fun demoDecodeImage(bytes: ByteArray): ImageBitmap? =
    runCatching { Image.makeFromEncoded(bytes).toComposeImageBitmap() }.getOrNull()
