// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.core.ui.componentes

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renan.mapaloja.core.ui.icones.IconesMapa

private val Laranja = Color(0xFFFFB74D)

/** Ícone + texto, para usar dentro de Button { ... }. */
@Composable
fun ConteudoBotao(icone: ImageVector, texto: String, estilo: TextStyle = LocalTextStyle.current) {
    Icon(icone, contentDescription = null, modifier = Modifier.size(22.dp))
    Spacer(Modifier.width(10.dp))
    Text(texto, style = estilo)
}

/** Retorno depois de bipar: verde quando deu certo, laranja quando precisa de atenção. */
@Composable
fun MensagemBipe(texto: String, alerta: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (alerta) IconesMapa.Alerta else IconesMapa.Sucesso,
            contentDescription = null,
            tint = if (alerta) Laranja else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(texto, fontWeight = FontWeight.SemiBold)
    }
}
