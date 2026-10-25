// SPDX-License-Identifier: GPL-3.0-or-later
// Módulo de tela (feature/*): biblioteca + Compose + acesso aos módulos core.

plugins {
    id("mapaloja.android.library")
    id("mapaloja.android.compose")
}

dependencies {
    "implementation"(project(":core:data"))
    "implementation"(project(":core:ui"))
    "implementation"(catalogo.findLibrary("androidx-navigation-compose").get())
    "implementation"(catalogo.findLibrary("androidx-lifecycle-runtime-compose").get())
    "implementation"(catalogo.findLibrary("androidx-activity-compose").get())
}
