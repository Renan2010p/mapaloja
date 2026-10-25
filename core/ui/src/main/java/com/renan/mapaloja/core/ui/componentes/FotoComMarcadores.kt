// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.core.ui.componentes

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.renan.mapaloja.core.data.fotos.Fotos
import com.renan.mapaloja.core.ui.tema.Verde
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class Marcador(val x: Float, val y: Float, val destaque: Boolean = true) {
    companion object {
        /** Cria o marcador só se o produto tiver posição marcada na foto. */
        fun de(x: Float?, y: Float?, destaque: Boolean = true): Marcador? =
            if (x != null && y != null) Marcador(x, y, destaque) else null
    }
}

@Composable
private fun rememberFoto(arquivo: File, maiorLado: Int): ImageBitmap? {
    val bitmap by produceState<ImageBitmap?>(null, arquivo.path, arquivo.lastModified()) {
        value = withContext(Dispatchers.IO) { Fotos.carregar(arquivo, maiorLado)?.asImageBitmap() }
    }
    return bitmap
}

/**
 * Mostra a foto da seção com os marcadores por cima.
 * Se [onToque] for passado, tocar na foto devolve a posição (0..1) do toque.
 */
@Composable
fun FotoComMarcadores(
    arquivo: File?,
    marcadores: List<Marcador>,
    modifier: Modifier = Modifier,
    onToque: ((Float, Float) -> Unit)? = null,
) {
    val formato = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
    if (arquivo == null || !arquivo.exists()) {
        Box(
            formato
                .height(140.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) { Text("Seção sem foto") }
        return
    }
    val foto = rememberFoto(arquivo, 1400)
    if (foto == null) {
        Box(formato.height(240.dp).background(MaterialTheme.colorScheme.surfaceVariant))
        return
    }

    val pulso by rememberInfiniteTransition(label = "pulso").animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pulso",
    )
    Box(
        formato
            .aspectRatio(foto.width.toFloat() / foto.height)
            .then(
                if (onToque != null) Modifier.pointerInput(onToque) {
                    detectTapGestures { p -> onToque(p.x / size.width, p.y / size.height) }
                } else Modifier
            )
    ) {
        Image(foto, contentDescription = "Foto da seção", Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
        Canvas(Modifier.fillMaxSize()) {
            marcadores.forEach { m ->
                val centro = Offset(m.x * size.width, m.y * size.height)
                if (m.destaque) {
                    drawCircle(Verde.copy(alpha = 0.4f), radius = 30.dp.toPx() * pulso, center = centro)
                    drawCircle(Color.Black, radius = 14.dp.toPx(), center = centro)
                    drawCircle(Color.White, radius = 12.dp.toPx(), center = centro)
                    drawCircle(Verde, radius = 9.dp.toPx(), center = centro)
                } else {
                    drawCircle(Color.Black, radius = 8.dp.toPx(), center = centro)
                    drawCircle(Verde, radius = 6.dp.toPx(), center = centro)
                }
            }
        }
    }
}

@Composable
fun Miniatura(arquivo: File?, tamanho: Dp = 56.dp) {
    val mod = Modifier
        .size(tamanho)
        .clip(RoundedCornerShape(8.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant)
    val foto = if (arquivo != null && arquivo.exists()) rememberFoto(arquivo, 200) else null
    if (foto == null) Box(mod) else Image(foto, null, mod, contentScale = ContentScale.Crop)
}
