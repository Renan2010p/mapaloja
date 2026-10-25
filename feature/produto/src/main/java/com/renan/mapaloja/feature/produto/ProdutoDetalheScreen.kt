// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.feature.produto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.renan.mapaloja.core.ui.componentes.FotoComMarcadores
import com.renan.mapaloja.core.ui.componentes.Marcador
import com.renan.mapaloja.core.ui.componentes.TelaBase
import com.renan.mapaloja.core.ui.navegacao.Rotas

/** O que aparece quando alguém bipa um produto: onde ele está. */
@Composable
internal fun ProdutoDetalheScreen(nav: NavController, id: Long) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    val local by remember { db.produtos().observarLocal(id) }.collectAsStateWithLifecycle(null)

    TelaBase(
        titulo = "Onde está",
        onVoltar = { nav.popBackStack() },
        acoes = {
            IconButton(onClick = { nav.navigate(Rotas.produtoEditar(id)) }) { Icon(Icons.Default.Edit, "Editar") }
        },
    ) { padding ->
        val r = local ?: return@TelaBase
        val p = r.produto
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(p.titulo(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            val codigos = listOfNotNull(
                p.codigoBarras?.let { "EAN $it" },
                p.codigoInterno?.let { "Cód. $it" },
            )
            if (codigos.isNotEmpty()) Text(codigos.joinToString("  •  "), color = MaterialTheme.colorScheme.onSurfaceVariant)

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (r.secaoNome == null) {
                        Text("Este produto ainda não tem local.", style = MaterialTheme.typography.titleMedium)
                    } else {
                        Text("📍 ${r.secaoNome}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        r.secaoLocalizacao?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = MaterialTheme.typography.titleMedium)
                        }
                        if (p.prateleira != null) {
                            Text("Prateleira ${p.prateleira} (contando de cima)", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            if (r.secaoNome != null) {
                FotoComMarcadores(
                    arquivo = r.secaoFoto?.let { Fotos.arquivo(ctx, it) },
                    marcadores = listOfNotNull(Marcador.de(p.posX, p.posY)),
                )
                if (r.secaoFoto != null && p.posX == null) {
                    Text(
                        "Dica: toque em ✏️ e depois na foto pra marcar o lugar exato.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedButton(onClick = { p.secaoId?.let { nav.navigate(Rotas.secao(it)) } }) { Text("Ver seção inteira") }
            }

            if (p.observacao.isNotBlank()) Text("Obs: ${p.observacao}")
        }
    }
}
