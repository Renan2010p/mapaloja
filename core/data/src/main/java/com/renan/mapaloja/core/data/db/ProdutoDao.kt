// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.core.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.renan.mapaloja.core.data.model.Produto
import com.renan.mapaloja.core.data.model.ProdutoLocal
import kotlinx.coroutines.flow.Flow

private const val SELECT_LOCAL = """
    SELECT p.*, s.nome AS secaoNome, s.localizacao AS secaoLocalizacao, s.fotoPath AS secaoFoto
    FROM produto p LEFT JOIN secao s ON s.id = p.secaoId
"""

@Dao
interface ProdutoDao {
    @Query(
        "$SELECT_LOCAL WHERE p.codigoBarras = :ean OR p.codigoInterno = :codigo " +
            "OR p.nome LIKE '%' || :texto || '%' ORDER BY p.nome COLLATE NOCASE LIMIT 200"
    )
    fun buscar(ean: String, codigo: String, texto: String): Flow<List<ProdutoLocal>>

    @Query("$SELECT_LOCAL WHERE p.id = :id")
    fun observarLocal(id: Long): Flow<ProdutoLocal?>

    @Query("SELECT * FROM produto WHERE secaoId = :secaoId ORDER BY prateleira, nome COLLATE NOCASE")
    fun daSecao(secaoId: Long): Flow<List<Produto>>

    @Query("SELECT COUNT(*) FROM produto")
    fun contar(): Flow<Int>

    /** Etiquetas a trocar, na ordem do caminho pela loja (seção, prateleira). */
    @Query(
        "$SELECT_LOCAL WHERE p.trocarEtiqueta = 1 " +
            "ORDER BY s.id IS NULL, s.localizacao COLLATE NOCASE, s.nome COLLATE NOCASE, p.prateleira, p.nome COLLATE NOCASE"
    )
    fun paraTrocarEtiqueta(): Flow<List<ProdutoLocal>>

    @Query("SELECT COUNT(*) FROM produto WHERE trocarEtiqueta = 1")
    fun contarEtiquetas(): Flow<Int>

    @Query("UPDATE produto SET trocarEtiqueta = :trocar WHERE id = :id")
    suspend fun marcarEtiqueta(id: Long, trocar: Boolean)

    @Query("UPDATE produto SET trocarEtiqueta = 0")
    suspend fun limparEtiquetas()

    @Query("SELECT * FROM produto WHERE codigoBarras = :ean LIMIT 1")
    suspend fun porEan(ean: String): Produto?

    @Query("SELECT * FROM produto WHERE codigoInterno = :codigo LIMIT 1")
    suspend fun porCodigoInterno(codigo: String): Produto?

    @Query("SELECT * FROM produto WHERE id = :id")
    suspend fun porId(id: Long): Produto?

    @Query("SELECT * FROM produto")
    suspend fun todos(): List<Produto>

    @Insert
    suspend fun inserir(produto: Produto): Long

    @Insert
    suspend fun inserirTodos(produtos: List<Produto>)

    @Update
    suspend fun atualizar(produto: Produto)

    @Delete
    suspend fun excluir(produto: Produto)

    @Query("DELETE FROM produto")
    suspend fun apagarTudo()
}
