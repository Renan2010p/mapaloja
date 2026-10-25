// SPDX-License-Identifier: GPL-3.0-or-later

plugins {
    id("mapaloja.android.library")
    id("mapaloja.android.compose")
}

android {
    namespace = "com.renan.mapaloja.core.ui"
}

dependencies {
    implementation(project(":core:data"))
    implementation(libs.play.services.code.scanner)
    implementation(libs.androidx.activity.compose)
}
