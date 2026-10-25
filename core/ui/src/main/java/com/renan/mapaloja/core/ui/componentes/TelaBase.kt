// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.core.ui.componentes

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.renan.mapaloja.core.ui.tema.Fundo

/** Estrutura padrão de todas as telas: barra de cima com título, voltar e ações. */
@Composable
fun TelaBase(
    titulo: String,
    onVoltar: (() -> Unit)? = null,
    acoes: @Composable RowScope.() -> Unit = {},
    botaoFlutuante: @Composable () -> Unit = {},
    conteudo: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    if (onVoltar != null) {
                        IconButton(onClick = onVoltar) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                },
                actions = acoes,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Fundo,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        floatingActionButton = botaoFlutuante,
        content = conteudo,
    )
}
