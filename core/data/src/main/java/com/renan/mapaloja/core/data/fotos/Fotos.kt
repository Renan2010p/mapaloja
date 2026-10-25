// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.core.data.fotos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import kotlin.math.max
import java.io.File

object Fotos {
    fun pasta(context: Context): File = File(context.filesDir, "fotos").apply { mkdirs() }

    fun arquivo(context: Context, nome: String): File = File(pasta(context), nome)

    fun arquivoOuNulo(context: Context, nome: String?): File? = nome?.let { arquivo(context, it) }

    fun novoArquivo(context: Context): File =
        File(pasta(context), "foto_${System.currentTimeMillis()}.jpg")

    /** Copia uma imagem da galeria para a pasta do app (já reduzida). Devolve null se não for uma imagem. */
    fun importar(context: Context, origem: Uri): File? {
        val destino = novoArquivo(context)
        return runCatching {
            context.contentResolver.openInputStream(origem)!!.use { entrada ->
                destino.outputStream().use { entrada.copyTo(it) }
            }
            checkNotNull(carregar(destino, 64))
            reduzir(destino)
            destino
        }.getOrElse {
            destino.delete()
            null
        }
    }

    fun uri(context: Context, arquivo: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fotos", arquivo)

    /** Lê a foto já reduzida, pra não estourar a memória do celular. */
    fun carregar(arquivo: File, maiorLado: Int): Bitmap? {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(arquivo.path, limites)
        if (limites.outWidth <= 0) return null
        var escala = 1
        while (max(limites.outWidth, limites.outHeight) / (escala * 2) >= maiorLado) escala *= 2
        return BitmapFactory.decodeFile(arquivo.path, BitmapFactory.Options().apply { inSampleSize = escala })
    }

    /**
     * Depois de tirar a foto: diminui o tamanho (fotos de câmera têm vários MB)
     * e já gira a imagem de acordo com o EXIF, pra ela aparecer sempre em pé.
     */
    fun reduzir(arquivo: File, maiorLado: Int = 2000) {
        val original = carregar(arquivo, maiorLado) ?: return
        val graus = when (
            ExifInterface(arquivo.path).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        val fator = maiorLado.toFloat() / max(original.width, original.height)
        val matriz = Matrix().apply {
            if (fator < 1f) postScale(fator, fator)
            postRotate(graus)
        }
        val final = Bitmap.createBitmap(original, 0, 0, original.width, original.height, matriz, true)
        arquivo.outputStream().use { final.compress(Bitmap.CompressFormat.JPEG, 85, it) }
    }
}
