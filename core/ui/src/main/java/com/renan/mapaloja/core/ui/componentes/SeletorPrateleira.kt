// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.core.ui.componentes

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Fileira de botões 1, 2, 3... para escolher a prateleira.
 * [quantidade] é quantas a gôndola tem; o "+" ([onAdicionar]) acrescenta mais uma.
 */
@Composable
fun SeletorPrateleira(
    valor: Int?,
    onMudar: (Int?) -> Unit,
    quantidade: Int,
    onAdicionar: (() -> Unit)? = null,
    permitirNenhuma: Boolean = false,
) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (permitirNenhuma) {
            FilterChip(selected = valor == null, onClick = { onMudar(null) }, label = { Text("—") })
        }
        (1..maxOf(quantidade, valor ?: 0)).forEach { n ->
            FilterChip(
                selected = valor == n,
                onClick = { onMudar(n) },
                label = { Text("$n", style = MaterialTheme.typography.titleMedium) },
            )
        }
        if (onAdicionar != null) {
            AssistChip(
                onClick = onAdicionar,
                label = { Icon(Icons.Default.Add, "Adicionar prateleira", Modifier.size(20.dp)) },
            )
        }
    }
}
