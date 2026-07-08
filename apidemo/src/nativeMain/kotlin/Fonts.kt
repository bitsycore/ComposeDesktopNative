package apidemo

import androidx.compose.ui.text.font.FontFamily
import com.compose.sdl.icons.IconFont
import com.compose.sdl.loadComposeResourceBytes
import com.compose.sdl.text.namedFontFamily

// ==================
// MARK: Monospace body font
// ==================

// Family name the body editor passes for monospace text. Resolves to the bundled
// Noto Sans Mono once registered; null means the font isn't in data.kres, in
// which case the body falls back to the default (proportional Noto Sans).
const val kMonoFamily = "noto-mono"

// The raw project family name string — used by the SyntaxHighlight tokeniser's
// wrap() call which measures via NativeTextMeasurer (accepts a name string).
val monoFontFamilyName: String? by lazy {
    val vBytes = loadComposeResourceBytes("font/NotoSansMono.ttf")
    if (vBytes == null) {
        println("apidemo: NotoSansMono.ttf not bundled — body uses the default font")
        null
    } else {
        IconFont.register(kMonoFamily, vBytes)
        kMonoFamily
    }
}

// The material3 Text / BasicTextField-shaped FontFamily wrapper. Wraps the
// registered project family in a `NamedFont`-backed FontFamily so the m3 Text
// signature (which takes `FontFamily?` proper) can be given the mono family.
val monoFontFamily: FontFamily? by lazy {
    monoFontFamilyName?.let { namedFontFamily(it) }
}
