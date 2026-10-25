// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.core.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.renan.mapaloja.core.data.model.Secao
import com.renan.mapaloja.core.data.model.SecaoComTotal
import kotlinx.coroutines.flow.Flow

@Dao
interface SecaoDao {
    @Query(
        "SELECT s.*, (SELECT COUNT(*) FROM produto p WHERE p.secaoId = s.id) AS total " +
            "FROM secao s ORDER BY s.nome COLLATE NOCASE"
    )
    fun observarComTotal(): Flow<List<SecaoComTotal>>

    @Query("SELECT * FROM secao ORDER BY nome COLLATE NOCASE")
    fun observarTodas(): Flow<List<Secao>>

    @Query("SELECT * FROM secao WHERE id = :id")
    fun observar(id: Long): Flow<Secao?>

    @Query("SELECT * FROM secao WHERE id = :id")
    suspend fun porId(id: Long): Secao?

    @Query("SELECT * FROM secao")
    suspend fun todas(): List<Secao>

    @Insert
    suspend fun inserir(secao: Secao): Long

    @Insert
    suspend fun inserirTodas(secoes: List<Secao>)

    @Update
    suspend fun atualizar(secao: Secao)

    @Delete
    suspend fun excluir(secao: Secao)

    @Query("DELETE FROM secao")
    suspend fun apagarTudo()
}
