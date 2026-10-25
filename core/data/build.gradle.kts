// SPDX-License-Identifier: GPL-3.0-or-later

plugins {
    id("mapaloja.android.library")
    id("mapaloja.android.room")
}

android {
    namespace = "com.renan.mapaloja.core.data"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.exifinterface)
}
