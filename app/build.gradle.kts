// SPDX-License-Identifier: GPL-3.0-or-later

plugins {
    id("mapaloja.android.application")
}

// Versão vem da tag no GitHub (v1.2.3 -> -PversaoApp=1.2.3); localmente usa a padrão.
val versaoApp = (findProperty("versaoApp") as String?) ?: "1.1.0"

/** "1.2.3" -> 10203. Cada versão nova precisa de um número maior para o Android aceitar a atualização. */
fun codigoDaVersao(versao: String): Int {
    val partes = versao.split('.', '-').mapNotNull { it.toIntOrNull() } + listOf(0, 0, 0)
    return partes[0] * 10000 + partes[1] * 100 + partes[2]
}

// Chave de assinatura: só existe na máquina do mantenedor e nos Secrets do GitHub.
val chave = System.getenv("MAPALOJA_KEYSTORE")?.let { file(it) }?.takeIf { it.exists() }

android {
    namespace = "com.renan.mapaloja"

    defaultConfig {
        applicationId = "com.renan.mapaloja"
        versionName = versaoApp
        versionCode = codigoDaVersao(versaoApp)
    }

    signingConfigs {
        if (chave != null) {
            create("release") {
                storeFile = chave
                storePassword = System.getenv("MAPALOJA_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("MAPALOJA_KEY_ALIAS")
                keyPassword = System.getenv("MAPALOJA_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Sem chave configurada, assina com a de debug só para o build não falhar
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":feature:busca"))
    implementation(project(":feature:etiquetas"))
    implementation(project(":feature:inicio"))
    implementation(project(":feature:produto"))
    implementation(project(":feature:secao"))
    implementation(project(":feature:sobre"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
}
