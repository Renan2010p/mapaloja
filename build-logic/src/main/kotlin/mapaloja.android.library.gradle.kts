// SPDX-License-Identifier: GPL-3.0-or-later
// Módulo de biblioteca Android (core/*, feature/*).

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = MapaLoja.COMPILE_SDK
    defaultConfig { minSdk = MapaLoja.MIN_SDK }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

configurarKotlin()
