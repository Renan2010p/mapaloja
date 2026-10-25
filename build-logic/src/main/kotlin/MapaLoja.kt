// SPDX-License-Identifier: GPL-3.0-or-later

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/** Configurações iguais para todos os módulos. */
object MapaLoja {
    const val COMPILE_SDK = 35
    const val MIN_SDK = 26
    const val TARGET_SDK = 35
}

val Project.catalogo: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun Project.configurarKotlin() {
    extensions.getByType<KotlinAndroidProjectExtension>().compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
    }
}
