// SPDX-License-Identifier: GPL-3.0-or-later

plugins {
    id("mapaloja.android.application")
}

android {
    namespace = "com.renan.mapaloja"

    defaultConfig {
        applicationId = "com.renan.mapaloja"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":feature:busca"))
    implementation(project(":feature:produto"))
    implementation(project(":feature:secao"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
}
