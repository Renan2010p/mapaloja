// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.core.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Uma área da loja: corredor, gôndola, ilha, balcão... */
@Entity(tableName = "secao")
data class Secao(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    /** Onde fica a seção na loja, ex: "Corredor 3, lado esquerdo". */
    val localizacao: String = "",
    /** Nome do arquivo da foto dentro da pasta de fotos do app. */
    val fotoPath: String? = null,
)

@Entity(
    tableName = "produto",
    foreignKeys = [
        ForeignKey(
            entity = Secao::class,
            parentColumns = ["id"],
            childColumns = ["secaoId"],
            onDelete = ForeignKey.SET_NULL,
        )
    ],
    indices = [
        Index(value = ["codigoBarras"], unique = true),
        Index("codigoInterno"),
        Index("secaoId"),
    ],
)
data class Produto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigoBarras: String? = null,
    val codigoInterno: String? = null,
    val nome: String = "",
    val secaoId: Long? = null,
    /** Número da prateleira, contando de cima pra baixo. */
    val prateleira: Int? = null,
    /** Posição na foto da seção, de 0 a 1 (fração da largura/altura). */
    val posX: Float? = null,
    val posY: Float? = null,
    val observacao: String = "",
    val atualizadoEm: Long = System.currentTimeMillis(),
) {
    fun titulo(): String = nome.ifBlank { codigoInterno ?: codigoBarras ?: "Produto sem nome" }
}

/** Produto junto com os dados da seção onde ele está. */
data class ProdutoLocal(
    @Embedded val produto: Produto,
    val secaoNome: String?,
    val secaoLocalizacao: String?,
    val secaoFoto: String?,
)

data class SecaoComTotal(
    @Embedded val secao: Secao,
    val total: Int,
)
