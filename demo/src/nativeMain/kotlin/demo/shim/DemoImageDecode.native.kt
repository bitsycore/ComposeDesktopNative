package demo.shim

import androidx.compose.ui.graphics.ImageBitmap
import com.compose.sdl.graphics.decodeEncodedImageBitmap

actual fun demoDecodeImage(bytes: ByteArray): ImageBitmap? = decodeEncodedImageBitmap(bytes)
