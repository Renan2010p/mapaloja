// SPDX-License-Identifier: GPL-3.0-or-later
// O módulo :app, que gera o APK.

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("mapaloja.android.compose")
}

android {
    compileSdk = MapaLoja.COMPILE_SDK
    defaultConfig {
        minSdk = MapaLoja.MIN_SDK
        targetSdk = MapaLoja.TARGET_SDK
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

configurarKotlin()
