// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.core.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.renan.mapaloja.core.data.model.Produto
import com.renan.mapaloja.core.data.model.Secao

@Database(entities = [Secao::class, Produto::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun produtos(): ProdutoDao
    abstract fun secoes(): SecaoDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun get(context: Context): AppDatabase = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "mapaloja.db",
            ).build().also { instancia = it }
        }
    }
}
