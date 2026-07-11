package demo.shim

import androidx.compose.ui.draganddrop.DragAndDropEvent
import java.awt.datatransfer.DataFlavor
import java.io.File

/* JVM (Compose Desktop) actual: DragAndDropEvent wraps an AWT
 * java.awt.dnd.DropTargetDropEvent whose transferable exposes the payload
 * through the standard AWT flavor system. */

actual fun DragAndDropEvent.demoReadFilePaths(): List<String> {
    val transferable = runCatching { nativeEvent.transferable }.getOrNull() ?: return emptyList()
    if (!transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) return emptyList()
    @Suppress("UNCHECKED_CAST")
    val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<File> ?: return emptyList()
    return files.map { it.absolutePath }
}

actual fun DragAndDropEvent.demoReadText(): String? {
    val transferable = runCatching { nativeEvent.transferable }.getOrNull() ?: return null
    if (!transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) return null
    return runCatching { transferable.getTransferData(DataFlavor.stringFlavor) as? String }.getOrNull()
}
