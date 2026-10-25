// SPDX-License-Identifier: GPL-3.0-or-later

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MapaDaLoja"

include(":app")
include(":core:data")
include(":core:ui")
include(":feature:busca")
include(":feature:etiquetas")
include(":feature:inicio")
include(":feature:produto")
include(":feature:secao")
include(":feature:sobre")
