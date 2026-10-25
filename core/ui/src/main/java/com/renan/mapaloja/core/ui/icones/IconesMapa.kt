// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.core.ui.icones

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Ícones próprios do app: traço de 2 px com pontas arredondadas, numa grade de 24×24.
 * Use com Icon(...): a cor vem do tint.
 */
object IconesMapa {
    /** Leitor de código de barras: cantos de mira + barras. */
    val Bipar = icone("Bipar") {
        moveTo(3f, 8f); lineTo(3f, 5f); quadTo(3f, 3f, 5f, 3f); lineTo(8f, 3f)
        moveTo(16f, 3f); lineTo(19f, 3f); quadTo(21f, 3f, 21f, 5f); lineTo(21f, 8f)
        moveTo(21f, 16f); lineTo(21f, 19f); quadTo(21f, 21f, 19f, 21f); lineTo(16f, 21f)
        moveTo(8f, 21f); lineTo(5f, 21f); quadTo(3f, 21f, 3f, 19f); lineTo(3f, 16f)
        linha(7f, 8f, 7f, 16f)
        linha(10f, 8f, 10f, 16f)
        linha(13.5f, 8f, 13.5f, 16f)
        linha(17f, 8f, 17f, 16f)
    }

    /** Etiqueta de preço. */
    val Etiqueta = icone("Etiqueta") {
        moveTo(3f, 4f); quadTo(3f, 3f, 4f, 3f); lineTo(11f, 3f); lineTo(21f, 13f)
        lineTo(13f, 21f); lineTo(3f, 11f); close()
        circulo(7.5f, 7.5f, 1.5f)
    }

    /** Gôndola com prateleiras. */
    val Estante = icone("Estante") {
        retangulo(3f, 3f, 18f, 18f, 2f)
        linha(3f, 9f, 21f, 9f)
        linha(3f, 15f, 21f, 15f)
        linha(7f, 6f, 7f, 9f)
        linha(13f, 12f, 13f, 15f)
        linha(10f, 18f, 10f, 21f)
    }

    val Camera = icone("Camera") {
        moveTo(4f, 7f); lineTo(7f, 7f); lineTo(9f, 4f); lineTo(15f, 4f); lineTo(17f, 7f); lineTo(20f, 7f)
        quadTo(21f, 7f, 21f, 8f); lineTo(21f, 19f); quadTo(21f, 20f, 20f, 20f); lineTo(4f, 20f)
        quadTo(3f, 20f, 3f, 19f); lineTo(3f, 8f); quadTo(3f, 7f, 4f, 7f); close()
        circulo(12f, 13.5f, 3.5f)
    }

    /** Imagem da galeria: moldura, sol e montanha. */
    val Galeria = icone("Galeria") {
        retangulo(3f, 3f, 18f, 18f, 2f)
        circulo(8.5f, 8.5f, 1.5f)
        moveTo(21f, 15f); lineTo(16f, 10f); lineTo(5f, 21f)
    }

    /** Pino de localização. */
    val Local = icone("Local") {
        moveTo(12f, 21f)
        curveTo(8f, 17f, 5f, 13.5f, 5f, 9.5f)
        arcTo(7f, 7f, 0f, false, true, 19f, 9.5f)
        curveTo(19f, 13.5f, 16f, 17f, 12f, 21f)
        close()
        circulo(12f, 9.5f, 2.5f)
    }

    val NovoProduto = icone("NovoProduto") {
        retangulo(3f, 3f, 18f, 18f, 3f)
        linha(12f, 8f, 12f, 16f)
        linha(8f, 12f, 16f, 12f)
    }

    val Backup = icone("Backup") {
        linha(12f, 3f, 12f, 15f)
        moveTo(7f, 10f); lineTo(12f, 15f); lineTo(17f, 10f)
        moveTo(4f, 16f); lineTo(4f, 19f); quadTo(4f, 21f, 6f, 21f); lineTo(18f, 21f)
        quadTo(20f, 21f, 20f, 19f); lineTo(20f, 16f)
    }

    val Celular = icone("Celular") {
        retangulo(6f, 2f, 12f, 20f, 2.5f)
        linha(11f, 18f, 13f, 18f)
    }

    /** Código aberto: </> */
    val Codigo = icone("Codigo") {
        moveTo(8f, 7f); lineTo(3f, 12f); lineTo(8f, 17f)
        moveTo(16f, 7f); lineTo(21f, 12f); lineTo(16f, 17f)
        linha(14f, 4f, 10f, 20f)
    }

    val Sucesso = icone("Sucesso") {
        circulo(12f, 12f, 9f)
        moveTo(8f, 12.5f); lineTo(11f, 15.5f); lineTo(16f, 9.5f)
    }

    val Alerta = icone("Alerta") {
        moveTo(12f, 3.5f); lineTo(21.5f, 20f); lineTo(2.5f, 20f); close()
        linha(12f, 9.5f, 12f, 13.5f)
        linha(12f, 16.8f, 12f, 16.9f)
    }
}

private fun icone(nome: String, desenho: PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = nome,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = desenho,
        )
    }.build()

private fun PathBuilder.linha(x1: Float, y1: Float, x2: Float, y2: Float) {
    moveTo(x1, y1)
    lineTo(x2, y2)
}

private fun PathBuilder.circulo(cx: Float, cy: Float, r: Float) {
    moveTo(cx - r, cy)
    arcToRelative(r, r, 0f, true, true, 2 * r, 0f)
    arcToRelative(r, r, 0f, true, true, -2 * r, 0f)
    close()
}

private fun PathBuilder.retangulo(x: Float, y: Float, largura: Float, altura: Float, raio: Float) {
    moveTo(x + raio, y)
    lineTo(x + largura - raio, y)
    quadTo(x + largura, y, x + largura, y + raio)
    lineTo(x + largura, y + altura - raio)
    quadTo(x + largura, y + altura, x + largura - raio, y + altura)
    lineTo(x + raio, y + altura)
    quadTo(x, y + altura, x, y + altura - raio)
    lineTo(x, y + raio)
    quadTo(x, y, x + raio, y)
    close()
}
