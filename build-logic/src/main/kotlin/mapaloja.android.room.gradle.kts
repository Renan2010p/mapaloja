// SPDX-License-Identifier: GPL-3.0-or-later
// Banco de dados Room (com o gerador de código KSP).

plugins {
    id("com.google.devtools.ksp")
}

ksp {
    // Guarda o esquema do banco em <módulo>/schemas para planejar migrações futuras
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    "api"(catalogo.findLibrary("room-runtime").get())
    "api"(catalogo.findLibrary("room-ktx").get())
    "ksp"(catalogo.findLibrary("room-compiler").get())
}
