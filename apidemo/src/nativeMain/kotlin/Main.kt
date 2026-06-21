package apidemo

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RoundedCornerShape
import androidx.compose.ui.platform.currentClipboard
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.desktop.native.nativeComposeWindow
import com.compose.desktop.native.showOpenFileDialog
import com.compose.desktop.native.showSaveFileDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ==================
// MARK: Theme — switchable dark / light palette
// ==================

class AppColors(
    val bg: Color,
    val panel: Color,
    val field: Color,
    val border: Color,
    val accent: Color,
    val text: Color,
    val dim: Color,
    val onAccent: Color,
)

private val DarkColors = AppColors(
    bg = Color(0xFF15161A),
    panel = Color(0xFF23252C),
    field = Color(0xFF2D2F37),
    border = Color(0xFF474C57),
    accent = Color(0xFF9F88FF),     // brighter violet — readable on dark
    text = Color(0xFFECEEF2),
    dim = Color(0xFFAEB4BD),
    onAccent = Color(0xFFFFFFFF),
)

private val LightColors = AppColors(
    bg = Color(0xFFF3F4F7),
    panel = Color(0xFFFFFFFF),
    field = Color(0xFFFFFFFF),
    border = Color(0xFFCED3DB),
    accent = Color(0xFF6B4BE6),
    text = Color(0xFF1B1D22),
    dim = Color(0xFF5C636E),
    onAccent = Color(0xFFFFFFFF),
)

private val LocalAppColors = staticCompositionLocalOf { DarkColors }

// ==================
// MARK: Entry point
// ==================

fun main() {
    nativeComposeWindow(title = "API Manager", width = 1240, height = 820) { Root() }
}

@Composable
private fun Root() {
    var vDark by remember { mutableStateOf(true) }
    val vC = if (vDark) DarkColors else LightColors
    val vMat = if (vDark) {
        darkColors(primary = vC.accent, background = vC.bg, surface = vC.panel, onPrimary = vC.onAccent, onBackground = vC.text, onSurface = vC.text)
    } else {
        lightColors(primary = vC.accent, background = vC.bg, surface = vC.panel, onPrimary = vC.onAccent, onBackground = vC.text, onSurface = vC.text)
    }
    MaterialTheme(colors = vMat) {
        CompositionLocalProvider(LocalAppColors provides vC) {
            App(vDark) { vDark = !vDark }
        }
    }
}

// ==================
// MARK: App
// ==================

@Composable
private fun App(inDark: Boolean, inOnToggleTheme: () -> Unit) {
    val c = LocalAppColors.current
    val vRunner = remember { HttpRunner() }
    val vScope = rememberCoroutineScope()

    val vRequests = remember { mutableStateListOf<ApiRequest>().apply { addAll(Pack().requests) } }
    var vSelected by remember { mutableStateOf(0) }
    var vPackName by remember { mutableStateOf("My Pack") }
    var vNotice by remember { mutableStateOf<String?>(null) }
    var vPackOpen by remember { mutableStateOf(false) }
    var vRenameIdx by remember { mutableStateOf(-1) }
    var vRenameText by remember { mutableStateOf("") }

    var vReqTab by remember { mutableStateOf(0) }
    var vRespTab by remember { mutableStateOf(0) }
    var vResponse by remember { mutableStateOf<ApiResponse?>(null) }
    var vLoading by remember { mutableStateOf(false) }

    fun edit(inT: (ApiRequest) -> ApiRequest) {
        if (vSelected in vRequests.indices) vRequests[vSelected] = inT(vRequests[vSelected])
    }

    Row(modifier = Modifier.fillMaxSize().background(c.bg)) {

        // ============
        //  Sidebar
        Column(
            modifier = Modifier.width(232.dp).fillMaxHeight().background(c.panel)
                .verticalScroll(rememberScrollState()).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(vPackName, color = c.text, fontSize = 17.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextChip("Pack…") { vPackOpen = true }
                OptionsMenu(inDark, inOnToggleTheme)
            }
            Divider(color = c.border)
            Text("REQUESTS", color = c.dim, fontSize = 11.sp)
            vRequests.forEachIndexed { vI, vReq ->
                val vSel = vI == vSelected
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp))
                        .background(if (vSel) c.accent.copy(alpha = 0.22f) else Color.Transparent, RoundedCornerShape(6.dp))
                        .clickable { vSelected = vI; vResponse = null }
                        .padding(horizontal = 8.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(vReq.method.name, color = methodColor(vReq.method), fontSize = 10.sp, modifier = Modifier.width(42.dp))
                    Text(vReq.name, color = c.text, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    TextChip("...") { vRenameIdx = vI; vRenameText = vReq.name }
                }
            }
            OutlinedButton(onClick = {
                vRequests.add(ApiRequest(name = "Request ${vRequests.size + 1}"))
                vSelected = vRequests.size - 1; vResponse = null
            }) { Text("+ New request", color = c.accent) }
        }

        // ============
        //  Main
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight()
                .verticalScroll(rememberScrollState()).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            val vReq = vRequests.getOrNull(vSelected)
            if (vReq == null) {
                Text("No request selected.", color = c.text)
            } else {
                Text(vReq.name, color = c.text, fontSize = 19.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    MethodPicker(vReq.method) { m -> edit { it.copy(method = m) } }
                    ThinField(vReq.url, { v -> edit { it.copy(url = v) } }, inModifier = Modifier.weight(1f), inPlaceholder = "https://example.com/path")
                    Button(
                        onClick = {
                            if (vLoading) return@Button
                            val vSend = vRequests[vSelected]
                            vLoading = true; vResponse = null
                            vScope.launch(Dispatchers.Main) {
                                val vR = withContext(Dispatchers.Default) { vRunner.run(vSend) }
                                vResponse = vR; vLoading = false
                            }
                        },
                    ) { Text(if (vLoading) "…" else "Send", color = c.onAccent) }
                }

                TabBar(listOf("Query (${vReq.params.size})", "Headers (${vReq.headers.size})", "Body"), vReqTab) { vReqTab = it }
                when (vReqTab) {
                    0 -> KeyValEditor(vReq.params) { v -> edit { it.copy(params = v) } }
                    1 -> KeyValEditor(vReq.headers) { v -> edit { it.copy(headers = v) } }
                    else -> BodyEditor(vReq) { v -> edit(v) }
                }

                Divider(color = c.border)
                ResponseView(vLoading, vResponse, vRespTab) { vRespTab = it }
            }
        }
    }

    if (vPackOpen) {
        PackDialog(
            inName = vPackName,
            inOnName = { vPackName = it },
            inNotice = vNotice,
            inOnExport = {
                showSaveFileDialog("${vPackName}.json") { vPath ->
                    if (vPath != null) {
                        val vErr = exportPack(Pack(vPackName, vRequests.toList()), vPath)
                        vNotice = vErr?.let { "Export failed: $it" } ?: "Saved ${vRequests.size} request(s)."
                    }
                }
            },
            inOnImport = {
                showOpenFileDialog { vPath ->
                    if (vPath != null) importPack(vPath).fold(
                        onSuccess = { vP ->
                            vPackName = vP.name
                            vRequests.clear(); vRequests.addAll(vP.requests)
                            vSelected = 0; vResponse = null
                            vNotice = "Imported ${vP.requests.size} request(s)."
                        },
                        onFailure = { vNotice = "Import failed: ${it.message}" },
                    )
                }
            },
            inOnClose = { vPackOpen = false },
        )
    }

    if (vRenameIdx in vRequests.indices) {
        Dialog(onDismissRequest = { vRenameIdx = -1 }) {
            Surface(color = c.panel, shape = RoundedCornerShape(10.dp), modifier = Modifier.width(360.dp)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Rename request", color = c.text, fontSize = 16.sp)
                    OutlinedTextField(vRenameText, { vRenameText = it }, modifier = Modifier.fillMaxWidth(), label = "Name", singleLine = true)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = {
                            val vIdx = vRenameIdx
                            if (vIdx in vRequests.indices && vRenameText.isNotBlank()) {
                                vRequests[vIdx] = vRequests[vIdx].copy(name = vRenameText.trim())
                            }
                            vRenameIdx = -1
                        }) { Text("Save", color = c.onAccent) }
                        OutlinedButton(onClick = { vRenameIdx = -1 }) { Text("Cancel", color = c.text) }
                    }
                }
            }
        }
    }
}

// ==================
// MARK: Pack dialog (sub-window)
// ==================

@Composable
private fun PackDialog(
    inName: String,
    inOnName: (String) -> Unit,
    inNotice: String?,
    inOnExport: () -> Unit,
    inOnImport: () -> Unit,
    inOnClose: () -> Unit,
) {
    val c = LocalAppColors.current
    Dialog(onDismissRequest = inOnClose) {
        Surface(color = c.panel, shape = RoundedCornerShape(10.dp), modifier = Modifier.width(420.dp)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Pack", color = c.text, fontSize = 18.sp)
                OutlinedTextField(inName, inOnName, modifier = Modifier.fillMaxWidth(), label = "Pack name", singleLine = true)
                Text("Export writes every request to a .json file via the native Save dialog; Import replaces the pack from a file.", color = c.dim, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = inOnExport) { Text("Export…", color = c.onAccent) }
                    OutlinedButton(onClick = inOnImport) { Text("Import…", color = c.accent) }
                }
                inNotice?.let { Text(it, color = c.dim, fontSize = 12.sp) }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = inOnClose) { Text("Close", color = c.text) }
                }
            }
        }
    }
}

// ==================
// MARK: Options menu (dark / light)
// ==================

@Composable
private fun OptionsMenu(inDark: Boolean, inOnToggleTheme: () -> Unit) {
    val c = LocalAppColors.current
    val vAnchor = rememberMenuAnchor()
    var vOpen by remember { mutableStateOf(false) }
    Box {
        TextChip("Options", Modifier.menuAnchor(vAnchor)) { vOpen = true }
        DropdownMenu(expanded = vOpen, onDismissRequest = { vOpen = false }, anchor = vAnchor) {
            DropdownMenuItem(onClick = { if (!inDark) inOnToggleTheme(); vOpen = false }) {
                Text("Dark mode", color = if (inDark) c.accent else c.text, fontSize = 13.sp)
            }
            DropdownMenuItem(onClick = { if (inDark) inOnToggleTheme(); vOpen = false }) {
                Text("Light mode", color = if (!inDark) c.accent else c.text, fontSize = 13.sp)
            }
        }
    }
}

// ==================
// MARK: Address-bar method picker (dropdown)
// ==================

@Composable
private fun MethodPicker(inMethod: ReqMethod, inOnPick: (ReqMethod) -> Unit) {
    val vAnchor = rememberMenuAnchor()
    var vOpen by remember { mutableStateOf(false) }
    Box {
        Box(
            modifier = Modifier.menuAnchor(vAnchor)
                .clip(RoundedCornerShape(6.dp))
                .background(methodColor(inMethod), RoundedCornerShape(6.dp))
                .clickable { vOpen = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) { Text(inMethod.name, color = Color.White, fontSize = 13.sp) }
        DropdownMenu(expanded = vOpen, onDismissRequest = { vOpen = false }, anchor = vAnchor) {
            ReqMethod.entries.forEach { vM ->
                DropdownMenuItem(onClick = { inOnPick(vM); vOpen = false }) {
                    Text(vM.name, color = methodColor(vM), fontSize = 13.sp)
                }
            }
        }
    }
}

// ==================
// MARK: Body editor
// ==================

@Composable
private fun BodyEditor(inReq: ApiRequest, inEdit: ((ApiRequest) -> ApiRequest) -> Unit) {
    val c = LocalAppColors.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (!inReq.method.allowsBody) {
            Text("${inReq.method.name} requests don't send a body.", color = c.dim, fontSize = 13.sp)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BodyType.entries.forEach { vT ->
                    TogglePill(vT.name, vT == inReq.bodyType) { inEdit { it.copy(bodyType = vT) } }
                }
            }
            if (inReq.bodyType != BodyType.NONE) {
                OutlinedTextField(
                    value = inReq.body,
                    onValueChange = { v -> inEdit { it.copy(body = v) } },
                    modifier = Modifier.fillMaxWidth().height(190.dp),
                    label = if (inReq.bodyType == BodyType.JSON) "JSON body" else "Body",
                )
            }
        }
    }
}

// ==================
// MARK: Response
// ==================

@Composable
private fun ResponseView(inLoading: Boolean, inResp: ApiResponse?, inTab: Int, inSelectTab: (Int) -> Unit) {
    val c = LocalAppColors.current
    var vMsg by remember { mutableStateOf<String?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        when {
            inLoading -> Text("Sending request…", color = c.text)
            inResp == null -> Text("Send a request to see the response.", color = c.dim, fontSize = 13.sp)
            inResp.error != null -> {
                StatusPill(0, "FAILED")
                CodeBox(inResp.error)
            }
            else -> {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(inResp.status, "${inResp.status} ${inResp.statusText}")
                    Text("${inResp.timeMs} ms", color = c.dim, fontSize = 13.sp)
                    Text("${inResp.sizeBytes} B", color = c.dim, fontSize = 13.sp)
                }
                TabBar(listOf("Body", "Headers (${inResp.headers.size})"), inTab, inSelectTab)
                val vBodyText = prettyJsonOrRaw(inResp.body).take(20000)
                val vHeaderText = inResp.headers.joinToString("\n") { (vK, vV) -> "$vK: $vV" }.ifEmpty { "(no headers)" }
                val vShown = if (inTab == 0) vBodyText else vHeaderText
                CodeActions(vShown, if (inTab == 0) "response.json" else "headers.txt") { vMsg = it }
                CodeBox(vShown)
                vMsg?.let { Text(it, color = c.dim, fontSize = 11.sp) }
            }
        }
    }
}

@Composable
private fun StatusPill(inStatus: Int, inLabel: String) {
    val vC = statusColor(inStatus)
    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(vC.copy(alpha = 0.20f), RoundedCornerShape(6.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(inLabel, color = vC, fontSize = 14.sp)
    }
}

@Composable
private fun CodeBox(inText: String) {
    val c = LocalAppColors.current
    Surface(color = c.bg, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
        // Read-only BasicTextField so the response is selectable + copyable (drag to select, Ctrl+C).
        BasicTextField(
            value = if (inText.isEmpty()) "(empty)" else inText,
            onValueChange = {},
            readOnly = true,
            color = c.text,
            cursorColor = c.accent,
            selectionColor = c.accent.copy(alpha = 0.35f),
            fontSize = 12.sp,
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
        )
    }
}

// ==================
// MARK: Key/value editor (single header, slim fields)
// ==================

@Composable
private fun KeyValEditor(inRows: List<KeyVal>, inOnChange: (List<KeyVal>) -> Unit) {
    val c = LocalAppColors.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (inRows.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Spacer(Modifier.width(46.dp))
                Text("KEY", color = c.dim, fontSize = 11.sp, modifier = Modifier.weight(1f))
                Text("VALUE", color = c.dim, fontSize = 11.sp, modifier = Modifier.weight(1.4f))
                Spacer(Modifier.width(34.dp))
            }
        }
        inRows.forEachIndexed { vI, vKv ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TogglePill(if (vKv.enabled) "on" else "off", vKv.enabled) {
                    inOnChange(inRows.mapIndexed { vJ, vR -> if (vJ == vI) vR.copy(enabled = !vR.enabled) else vR })
                }
                ThinField(vKv.key, { v -> inOnChange(inRows.mapIndexed { vJ, vR -> if (vJ == vI) vR.copy(key = v) else vR }) }, inModifier = Modifier.weight(1f), inPlaceholder = "key")
                ThinField(vKv.value, { v -> inOnChange(inRows.mapIndexed { vJ, vR -> if (vJ == vI) vR.copy(value = v) else vR }) }, inModifier = Modifier.weight(1.4f), inPlaceholder = "value")
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable { inOnChange(inRows.filterIndexed { vJ, _ -> vJ != vI }) }.padding(horizontal = 10.dp, vertical = 10.dp)) {
                    Text("x", color = MaterialTheme.colors.error, fontSize = 14.sp)
                }
            }
        }
        OutlinedButton(onClick = { inOnChange(inRows + KeyVal()) }) { Text("+ Add row", color = c.accent) }
    }
}

// ==================
// MARK: Small reusable bits
// ==================

@Composable
private fun TabBar(inTabs: List<String>, inSelected: Int, inOnSelect: (Int) -> Unit) {
    val c = LocalAppColors.current
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        inTabs.forEachIndexed { vI, vT ->
            val vSel = vI == inSelected
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.clickable { inOnSelect(vI) }.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(vT, color = if (vSel) c.accent else c.dim, fontSize = 13.sp)
                }
                Box(modifier = Modifier.height(2.dp).width(if (vSel) 28.dp else 0.dp).background(c.accent))
            }
        }
    }
}

@Composable
private fun TogglePill(inLabel: String, inSelected: Boolean, inOnClick: () -> Unit) {
    val c = LocalAppColors.current
    Box(
        modifier = Modifier.clip(RoundedCornerShape(6.dp))
            .background(if (inSelected) c.accent else c.field, RoundedCornerShape(6.dp))
            .border(1.dp, c.border, RoundedCornerShape(6.dp))
            .clickable(onClick = inOnClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) { Text(inLabel, color = if (inSelected) c.onAccent else c.dim, fontSize = 12.sp) }
}

@Composable
private fun TextChip(inLabel: String, inModifier: Modifier = Modifier, inOnClick: () -> Unit) {
    val c = LocalAppColors.current
    Box(modifier = inModifier.clip(RoundedCornerShape(6.dp)).border(1.dp, c.border, RoundedCornerShape(6.dp)).clickable(onClick = inOnClick).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(inLabel, color = c.dim, fontSize = 12.sp)
    }
}

/* Compact single-line input — a BasicTextField in a slim bordered box, much
   shorter than the Material OutlinedTextField (which is fixed at 56 dp). */
@Composable
private fun ThinField(inValue: String, inOnChange: (String) -> Unit, inModifier: Modifier = Modifier, inPlaceholder: String = "") {
    val c = LocalAppColors.current
    Box(
        modifier = inModifier.clip(RoundedCornerShape(6.dp))
            .background(c.field, RoundedCornerShape(6.dp))
            .border(1.dp, c.border, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (inValue.isEmpty() && inPlaceholder.isNotEmpty()) Text(inPlaceholder, color = c.dim, fontSize = 13.sp)
        BasicTextField(
            value = inValue,
            onValueChange = inOnChange,
            color = c.text,
            cursorColor = c.accent,
            selectionColor = c.accent.copy(alpha = 0.35f),
            fontSize = 13.sp,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/* Copy-to-clipboard + native Save-as row, used on the response body / headers. */
@Composable
private fun CodeActions(inText: String, inDefaultName: String, inOnMsg: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        TextChip("Copy") { currentClipboard.setText(inText); inOnMsg("Copied to clipboard.") }
        TextChip("Save as…") {
            showSaveFileDialog(inDefaultName) { vPath ->
                if (vPath != null) inOnMsg(writeTextFile(vPath, inText)?.let { "Save failed: $it" } ?: "Saved to $vPath")
            }
        }
    }
}

private fun methodColor(inM: ReqMethod): Color = when (inM) {
    ReqMethod.GET -> Color(0xFF4C9AFF)
    ReqMethod.POST -> Color(0xFF36B37E)
    ReqMethod.PUT -> Color(0xFFFF991F)
    ReqMethod.PATCH -> Color(0xFF00B8D9)
    ReqMethod.DELETE -> Color(0xFFFF5630)
    ReqMethod.HEAD, ReqMethod.OPTIONS -> Color(0xFF8777FF)
}

private fun statusColor(inStatus: Int): Color = when (inStatus) {
    in 200..299 -> Color(0xFF36B37E)
    in 300..399 -> Color(0xFF4C9AFF)
    in 400..599 -> Color(0xFFFF5630)
    else -> Color(0xFFFF991F)
}
