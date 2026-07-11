package demo.shim

import androidx.compose.ui.draganddrop.DragAndDropEvent

actual fun DragAndDropEvent.demoReadFilePaths(): List<String> = transferData.filePaths
actual fun DragAndDropEvent.demoReadText(): String? = transferData.text
