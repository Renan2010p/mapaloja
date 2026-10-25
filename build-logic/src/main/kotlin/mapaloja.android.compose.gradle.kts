// SPDX-License-Identifier: GPL-3.0-or-later
// Liga o Jetpack Compose num módulo (app ou biblioteca).

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

pluginManager.withPlugin("com.android.library") {
    extensions.configure<com.android.build.api.dsl.LibraryExtension> { buildFeatures.compose = true }
}
pluginManager.withPlugin("com.android.application") {
    extensions.configure<com.android.build.api.dsl.ApplicationExtension> { buildFeatures.compose = true }
}

dependencies {
    "implementation"(platform(catalogo.findLibrary("compose-bom").get()))
    "implementation"(catalogo.findLibrary("compose-ui").get())
    "implementation"(catalogo.findLibrary("compose-material3").get())
    "implementation"(catalogo.findLibrary("compose-material-icons-core").get())
}
