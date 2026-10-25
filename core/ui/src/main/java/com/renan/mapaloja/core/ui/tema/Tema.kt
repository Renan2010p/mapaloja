// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.core.ui.tema

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val Verde = Color(0xFF2ECC71)
val Fundo = Color(0xFF0E1311)

@Composable
fun TemaMapa(conteudo: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Verde,
            onPrimary = Color(0xFF00210F),
            primaryContainer = Color(0xFF123D27),
            onPrimaryContainer = Color(0xFFB9F6CF),
            secondary = Color(0xFF7FD8A8),
            secondaryContainer = Color(0xFF1E2B25),
            onSecondaryContainer = Color(0xFFD2E8DB),
            background = Fundo,
            onBackground = Color(0xFFE3E8E5),
            surface = Fundo,
            onSurface = Color(0xFFE3E8E5),
            surfaceVariant = Color(0xFF1C2420),
            onSurfaceVariant = Color(0xFF9DAAA3),
            surfaceContainerHighest = Color(0xFF1C2420),
            outline = Color(0xFF3A4741),
        ),
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(18.dp),
            large = RoundedCornerShape(24.dp),
        ),
        content = conteudo,
    )
}
