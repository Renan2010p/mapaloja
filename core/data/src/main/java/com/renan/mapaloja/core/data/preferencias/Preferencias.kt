// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.core.data.preferencias

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Configurações simples do app, guardadas no celular. */
class Preferencias private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE)

    private val _nomeLoja = MutableStateFlow(prefs.getString(NOME_LOJA, "") ?: "")
    val nomeLoja: StateFlow<String> = _nomeLoja.asStateFlow()

    val boasVindasConcluida: Boolean
        get() = prefs.getBoolean(BOAS_VINDAS, false)

    fun concluirBoasVindas(nomeLoja: String) {
        prefs.edit {
            putBoolean(BOAS_VINDAS, true)
            putString(NOME_LOJA, nomeLoja.trim())
        }
        _nomeLoja.value = nomeLoja.trim()
    }

    companion object {
        private const val BOAS_VINDAS = "boas_vindas_concluida"
        private const val NOME_LOJA = "nome_loja"

        @Volatile
        private var instancia: Preferencias? = null

        fun get(context: Context): Preferencias = instancia ?: synchronized(this) {
            instancia ?: Preferencias(context.applicationContext).also { instancia = it }
        }
    }
}
