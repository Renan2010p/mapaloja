// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.core.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.renan.mapaloja.core.data.db.AppDatabase
import com.renan.mapaloja.core.data.fotos.Fotos
import com.renan.mapaloja.core.data.model.Produto
import com.renan.mapaloja.core.data.model.Secao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Salva/restaura o mapa inteiro (seções, produtos e fotos) num arquivo .zip.
 * Importante porque tudo fica só no celular da loja: se ele quebrar, o backup salva o trabalho.
 */
object Backup {
    private const val DADOS = "dados.json"

    suspend fun exportar(context: Context, destino: Uri) = withContext(Dispatchers.IO) {
        val db = AppDatabase.get(context)
        val json = JSONObject()
            .put("versao", 1)
            .put("secoes", JSONArray().apply {
                db.secoes().todas().forEach {
                    put(
                        JSONObject()
                            .put("id", it.id)
                            .put("nome", it.nome)
                            .put("localizacao", it.localizacao)
                            .put("foto", it.fotoPath ?: JSONObject.NULL)
                    )
                }
            })
            .put("produtos", JSONArray().apply {
                db.produtos().todos().forEach {
                    put(
                        JSONObject()
                            .put("id", it.id)
                            .put("codigoBarras", it.codigoBarras ?: JSONObject.NULL)
                            .put("codigoInterno", it.codigoInterno ?: JSONObject.NULL)
                            .put("nome", it.nome)
                            .put("secaoId", it.secaoId ?: JSONObject.NULL)
                            .put("prateleira", it.prateleira ?: JSONObject.NULL)
                            .put("posX", it.posX?.toDouble() ?: JSONObject.NULL)
                            .put("posY", it.posY?.toDouble() ?: JSONObject.NULL)
                            .put("observacao", it.observacao)
                            .put("atualizadoEm", it.atualizadoEm)
                    )
                }
            })

        val saida = context.contentResolver.openOutputStream(destino)
            ?: error("Não foi possível criar o arquivo")
        ZipOutputStream(saida).use { zip ->
            zip.putNextEntry(ZipEntry(DADOS))
            zip.write(json.toString().toByteArray())
            zip.closeEntry()
            Fotos.pasta(context).listFiles()?.forEach { foto ->
                zip.putNextEntry(ZipEntry("fotos/${foto.name}"))
                foto.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    /** Substitui tudo que está no app pelo conteúdo do backup. Retorna (seções, produtos). */
    suspend fun importar(context: Context, origem: Uri): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val temp = File(context.cacheDir, "importacao").apply { deleteRecursively(); mkdirs() }
        var texto: String? = null

        val entrada = context.contentResolver.openInputStream(origem)
            ?: error("Não foi possível abrir o arquivo")
        ZipInputStream(entrada).use { zip ->
            while (true) {
                val item = zip.nextEntry ?: break
                when {
                    item.name == DADOS -> texto = zip.readBytes().decodeToString()
                    item.name.startsWith("fotos/") && !item.isDirectory ->
                        // .name evita que um zip malicioso escreva fora da pasta
                        File(temp, File(item.name).name).outputStream().use { zip.copyTo(it) }
                }
            }
        }
        val json = JSONObject(texto ?: error("Esse arquivo não é um backup do Mapa da Loja"))

        val secoes = json.getJSONArray("secoes").objetos().map {
            Secao(
                id = it.getLong("id"),
                nome = it.getString("nome"),
                localizacao = it.optString("localizacao"),
                fotoPath = it.textoOuNulo("foto"),
            )
        }
        val produtos = json.getJSONArray("produtos").objetos().map {
            Produto(
                id = it.getLong("id"),
                codigoBarras = it.textoOuNulo("codigoBarras"),
                codigoInterno = it.textoOuNulo("codigoInterno"),
                nome = it.optString("nome"),
                secaoId = if (it.isNull("secaoId")) null else it.getLong("secaoId"),
                prateleira = if (it.isNull("prateleira")) null else it.getInt("prateleira"),
                posX = if (it.isNull("posX")) null else it.getDouble("posX").toFloat(),
                posY = if (it.isNull("posY")) null else it.getDouble("posY").toFloat(),
                observacao = it.optString("observacao"),
                atualizadoEm = it.optLong("atualizadoEm", System.currentTimeMillis()),
            )
        }

        val db = AppDatabase.get(context)
        db.withTransaction {
            db.produtos().apagarTudo()
            db.secoes().apagarTudo()
            db.secoes().inserirTodas(secoes)
            db.produtos().inserirTodos(produtos)
        }

        val pasta = Fotos.pasta(context)
        pasta.listFiles()?.forEach { it.delete() }
        temp.listFiles()?.forEach { it.copyTo(File(pasta, it.name), overwrite = true) }
        temp.deleteRecursively()

        secoes.size to produtos.size
    }

    private fun JSONArray.objetos(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }

    private fun JSONObject.textoOuNulo(chave: String): String? = if (isNull(chave)) null else getString(chave)
}
