// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.feature.secao

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.renan.mapaloja.core.data.db.AppDatabase
import com.renan.mapaloja.core.data.fotos.Fotos
import com.renan.mapaloja.core.ui.componentes.Miniatura
import com.renan.mapaloja.core.ui.componentes.TelaBase
import com.renan.mapaloja.core.ui.navegacao.Rotas

@Composable
internal fun SecoesScreen(nav: NavController) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    val secoes by remember { db.secoes().observarComTotal() }.collectAsStateWithLifecycle(emptyList())

    TelaBase(
        titulo = "Seções da loja",
        onVoltar = { nav.popBackStack() },
        botaoFlutuante = {
            ExtendedFloatingActionButton(
                onClick = { nav.navigate(Rotas.secaoEditar(0)) },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nova seção") },
            )
        },
    ) { padding ->
        if (secoes.isEmpty()) {
            Column(Modifier.padding(padding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Nenhuma seção ainda.", style = MaterialTheme.typography.titleMedium)
                Text("Comece criando as seções da loja (ex: \"Corredor 1 - Vitaminas\") e tire uma foto de cada uma.")
            }
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = padding) {
            items(secoes, key = { it.secao.id }) { item ->
                ListItem(
                    modifier = Modifier.clickable { nav.navigate(Rotas.secao(item.secao.id)) },
                    leadingContent = { Miniatura(item.secao.fotoPath?.let { Fotos.arquivo(ctx, it) }) },
                    headlineContent = { Text(item.secao.nome, fontWeight = FontWeight.SemiBold) },
                    supportingContent = {
                        Text(listOf(item.secao.localizacao, if (item.total == 1) "1 produto" else "${item.total} produtos").filter { it.isNotBlank() }.joinToString(" • "))
                    },
                )
                HorizontalDivider()
            }
        }
    }
}
