// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.core.data.importacao

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.renan.mapaloja.core.data.db.AppDatabase
import com.renan.mapaloja.core.data.model.Produto
import com.renan.mapaloja.core.data.model.normalizarCodigo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.Charset

/**
 * Importa uma planilha de produtos salva como CSV (Excel, LibreOffice, Google Planilhas).
 *
 * Não precisa de um formato fixo: em cada linha, o campo com 8 a 14 dígitos é o código
 * de barras, o campo mais longo com letras é o nome, e o que sobrar é o código interno.
 * Produtos que já existem ganham nome/código se estiverem sem; o local nunca é alterado.
 */
object ImportarCsv {
    data class Resultado(val novos: Int, val atualizados: Int, val ignorados: Int)

    suspend fun importar(context: Context, origem: Uri): Resultado = withContext(Dispatchers.IO) {
        val bytes = context.contentResolver.openInputStream(origem)?.use { it.readBytes() }
            ?: error("Não foi possível abrir o arquivo")
        val linhas = decodificar(bytes).lines().filter { it.isNotBlank() }
        val separador = listOf(';', '\t', ',').maxBy { s -> linhas.take(20).sumOf { l -> l.count { it == s } } }

        val db = AppDatabase.get(context)
        var novos = 0
        var atualizados = 0
        var ignorados = 0
        db.withTransaction {
            for (linha in linhas) {
                val item = interpretar(linha.split(separador).map { it.trim().trim('"').trim() })
                if (item == null) {
                    ignorados++
                    continue
                }
                val existente = db.produtos().porEan(item.ean)
                if (existente == null) {
                    db.produtos().inserir(Produto(codigoBarras = item.ean, codigoInterno = item.codigo, nome = item.nome))
                    novos++
                } else if ((existente.nome.isBlank() && item.nome.isNotBlank()) ||
                    (existente.codigoInterno == null && item.codigo != null)
                ) {
                    db.produtos().atualizar(
                        existente.copy(
                            nome = existente.nome.ifBlank { item.nome },
                            codigoInterno = existente.codigoInterno ?: item.codigo,
                        )
                    )
                    atualizados++
                } else {
                    ignorados++
                }
            }
        }
        Resultado(novos, atualizados, ignorados)
    }

    private data class Item(val ean: String, val nome: String, val codigo: String?)

    private fun interpretar(campos: List<String>): Item? {
        val ean = campos.firstOrNull { c -> c.length in 8..14 && c.all { it.isDigit() } } ?: return null
        val nome = campos.filter { c -> c.any { it.isLetter() } }.maxByOrNull { it.length } ?: ""
        val codigo = campos.firstOrNull { it.isNotEmpty() && it != ean && it != nome }
        return Item(normalizarCodigo(ean), nome, codigo)
    }

    /** Planilhas do Excel no Brasil costumam vir em Windows-1252, não em UTF-8. */
    private fun decodificar(bytes: ByteArray): String {
        val utf8 = bytes.decodeToString().removePrefix("﻿")
        return if ('�' in utf8) String(bytes, Charset.forName("windows-1252")) else utf8
    }
}
