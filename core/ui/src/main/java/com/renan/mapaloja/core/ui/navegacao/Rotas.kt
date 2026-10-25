// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.core.ui.navegacao

import android.net.Uri

/**
 * Endereços das telas. Fica num lugar só para que um módulo de tela
 * possa abrir a tela de outro módulo sem depender dele.
 */
object Rotas {
    const val BUSCA = "busca"

    const val PRODUTO = "produto/{id}"
    fun produto(id: Long) = "produto/$id"

    const val PRODUTO_EDITAR = "produtoEditar/{id}?ean={ean}&secao={secao}"
    fun produtoEditar(id: Long = 0, ean: String? = null, secao: Long = 0): String {
        val parametros = listOfNotNull(
            ean?.let { "ean=${Uri.encode(it)}" },
            secao.takeIf { it > 0 }?.let { "secao=$it" },
        )
        return "produtoEditar/$id" + if (parametros.isEmpty()) "" else parametros.joinToString("&", prefix = "?")
    }

    const val SECOES = "secoes"

    const val SECAO = "secao/{id}"
    fun secao(id: Long) = "secao/$id"

    const val SECAO_EDITAR = "secaoEditar/{id}"
    fun secaoEditar(id: Long) = "secaoEditar/$id"
}
