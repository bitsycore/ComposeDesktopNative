// :material-symbols:rounded — Rounded style of Material Symbols. Mirrors
// the :outlined module but ships a different .ttf and registers under a
// different family name. See :outlined for architectural notes.
// Publication artifactId (when set up): compose-desktop-material-symbols-rounded.

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.net.URI

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.compose)
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

val vHostOs = System.getProperty("os.name")
val vIsMacHost = vHostOs.startsWith("Mac")
val vIsLinuxHost = vHostOs == "Linux"
val vIsWindowsHost = vHostOs.startsWith("Windows")

kotlin {
    if (vIsLinuxHost) { linuxArm64(); linuxX64() }
    if (vIsMacHost) macosArm64()
    if (vIsWindowsHost) mingwX64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            api(project(":core"))
        }
    }
}

val materialSymbolsFont = layout.buildDirectory.file("iconFont/MaterialSymbolsRounded.ttf")

val downloadMaterialSymbolsRounded = tasks.register("downloadMaterialSymbolsRounded") {
    val vUrl = "https://github.com/google/material-design-icons/raw/master/variablefont/MaterialSymbolsRounded%5BFILL%2CGRAD%2Copsz%2Cwght%5D.ttf"
    val vOut = materialSymbolsFont.get().asFile
    outputs.file(vOut)
    doLast {
        if (vOut.exists() && vOut.length() > 0) return@doLast
        vOut.parentFile.mkdirs()
        println("Downloading $vUrl")
        URI(vUrl).toURL().openStream().use { vIn ->
            vOut.outputStream().use { vIn.copyTo(it) }
        }
        println("Saved ${vOut.length() / 1024} KiB to $vOut")
    }
}

extra["iconFontFile"] = materialSymbolsFont
extra["iconFontDownloadTask"] = downloadMaterialSymbolsRounded
