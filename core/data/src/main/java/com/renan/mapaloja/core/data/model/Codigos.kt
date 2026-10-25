// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.core.data.model

/**
 * Leitores às vezes devolvem o mesmo EAN com ou sem zero na frente (UPC-A x EAN-13),
 * então guardamos códigos de barras numéricos sem os zeros iniciais.
 */
fun normalizarCodigo(codigo: String): String {
    val c = codigo.trim()
    return if (c.length in 8..14 && c.all { it.isDigit() }) c.trimStart('0') else c
}

fun textoLocal(secaoNome: String?, localizacao: String?, prateleira: Int?): String {
    if (secaoNome == null) return "Sem local definido"
    return buildString {
        append(secaoNome)
        if (!localizacao.isNullOrBlank()) append(" • ").append(localizacao)
        if (prateleira != null) append(" • Prateleira ").append(prateleira)
    }
}
