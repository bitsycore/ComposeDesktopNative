@file:Suppress("PropertyName", "ConstPropertyName")

package com.compose.sdl.icons.material.symbols

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.unit.Dp
import com.compose.sdl.icons.IconDefaults
import com.compose.sdl.icons.IconFont
import com.compose.sdl.icons.IconFontIcon
import com.compose.sdl.loadComposeResourceBytes
import com.compose.sdl.text.TextRendererCapabilities

sealed class MaterialSymbolsIcon {

    abstract val Family: String
    abstract val DefaultResourcePath: String

    private var isInstalled = false

    internal fun install(resourcePath: String = DefaultResourcePath): Boolean {
        if (isInstalled) return true
        val vBytes = loadComposeResourceBytes(resourcePath)
        if (vBytes == null) {
            println(
                "$Family: font missing at \"$resourcePath\" in data.kres. " +
                        "Add the font to your app's resources."
            )
            return false
        }
        IconFont.registerIcon(Family, vBytes)
        isInstalled = true
        if (TextRendererCapabilities.supportsFontVariations == false) {
            println(
                "$Family: the active text renderer does not support variable-font axes. " +
                        "Icons will render at the font's default position (wght=${MaterialIconAxisDefaults.Weight}, FILL=${MaterialIconAxisDefaults.Fill}, GRAD=${MaterialIconAxisDefaults.Grade}, opsz=${MaterialIconAxisDefaults.OpticalSize}). " +
                        "fill / weight / grade / opticalSize parameters are ignored."
            )
        }
        return true
    }

    @Composable
    operator fun invoke(
        icon: Int,
        contentDescription: String? = null,
        modifier: Modifier = Modifier,
        tint: Color = Color.Unspecified,
        size: Dp = IconDefaults.DefaultIconSize,
        fill: Float = MaterialIconAxisDefaults.Fill,
        weight: Int = MaterialIconAxisDefaults.Weight,
        grade: Int = MaterialIconAxisDefaults.Grade,
        opticalSize: Int = MaterialIconAxisDefaults.OpticalSize,
    ) {
        install()
        IconFontIcon(
            codepoint = icon,
            fontFamily = Family,
            contentDescription = contentDescription,
            modifier = modifier,
            tint = if (tint.isSpecified) tint else LocalContentColor.current,
            size = size,
            fontVariationSettings = materialIconAxes(fill, weight, grade, opticalSize),
        )
    }

}

object MaterialSymbolsOutlined : MaterialSymbolsIcon() {
    override val Family: String = "material-symbols-outlined"
    override val DefaultResourcePath: String = "font/MaterialSymbolsOutlined.ttf"
}

object MaterialSymbolsSharp : MaterialSymbolsIcon() {
    override val Family: String = "material-symbols-sharp"
    override val DefaultResourcePath: String = "font/MaterialSymbolsSharp.ttf"
}

object MaterialSymbolsRounded : MaterialSymbolsIcon() {
    override val Family: String = "material-symbols-rounded"
    override val DefaultResourcePath: String = "font/MaterialSymbolsRounded.ttf"
}

object MaterialIconAxisDefaults {
	const val Fill: Float = 0f
	const val Weight: Int = 400
	const val Grade: Int = 0
	const val OpticalSize: Int = 24
}

private fun materialIconAxes(
	fill: Float = MaterialIconAxisDefaults.Fill,
	weight: Int = MaterialIconAxisDefaults.Weight,
	grade: Int = MaterialIconAxisDefaults.Grade,
	opticalSize: Int = MaterialIconAxisDefaults.OpticalSize,
): List<FontVariation.Setting> {
	if (fill == MaterialIconAxisDefaults.Fill &&
		weight == MaterialIconAxisDefaults.Weight &&
		grade == MaterialIconAxisDefaults.Grade &&
		opticalSize == MaterialIconAxisDefaults.OpticalSize
	) return emptyList()
	return listOf(
		FontVariation.Setting("FILL", fill),
		FontVariation.Setting("wght", weight.toFloat()),
		FontVariation.Setting("GRAD", grade.toFloat()),
		FontVariation.Setting("opsz", opticalSize.toFloat()),
	)
}