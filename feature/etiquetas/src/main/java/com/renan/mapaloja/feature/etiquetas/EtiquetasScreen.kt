// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.feature.etiquetas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.renan.mapaloja.core.data.db.AppDatabase
import com.renan.mapaloja.core.data.fotos.Fotos
import com.renan.mapaloja.core.data.model.Produto
import com.renan.mapaloja.core.data.model.normalizarCodigo
import com.renan.mapaloja.core.ui.componentes.ConteudoBotao
import com.renan.mapaloja.core.ui.componentes.MensagemBipe
import com.renan.mapaloja.core.ui.componentes.Miniatura
import com.renan.mapaloja.core.ui.componentes.TelaBase
import com.renan.mapaloja.core.ui.icones.IconesMapa
import com.renan.mapaloja.core.ui.leitor.lerCodigo
import com.renan.mapaloja.core.ui.navegacao.Rotas
import kotlinx.coroutines.launch

/**
 * Bipa os produtos que mudaram de preço e mostra a lista agrupada por seção,
 * na ordem do caminho pela loja. Marcar o quadradinho tira o produto da lista.
 */
@Composable
internal fun EtiquetasScreen(nav: NavController) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    val scope = rememberCoroutineScope()
    val vibrar = LocalHapticFeedback.current
    val lista by remember { db.produtos().paraTrocarEtiqueta() }.collectAsStateWithLifecycle(emptyList())

    var emSequencia by rememberSaveable { mutableStateOf(true) }
    // Texto do último bipe e se ele precisa de atenção (produto ainda não mapeado)
    var ultimo by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var confirmarLimpar by remember { mutableStateOf(false) }

    fun bipar() {
        lerCodigo(ctx) { lido ->
            scope.launch {
                val ean = normalizarCodigo(lido)
                val produto = db.produtos().porEan(ean) ?: db.produtos().porCodigoInterno(lido.trim())
                ultimo = if (produto == null) {
                    // Entra na lista mesmo sem local, para não esquecer; dá pra mapear depois
                    db.produtos().inserir(Produto(codigoBarras = ean, trocarEtiqueta = true))
                    "$ean ainda não está mapeado. Entrou na lista sem local." to true
                } else {
                    db.produtos().marcarEtiqueta(produto.id, true)
                    produto.titulo() to false
                }
                vibrar.performHapticFeedback(HapticFeedbackType.LongPress)
                if (emSequencia) bipar()
            }
        }
    }

    TelaBase(
        titulo = "Trocar etiquetas",
        onVoltar = { nav.popBackStack() },
        acoes = {
            if (lista.isNotEmpty()) {
                IconButton(onClick = { confirmarLimpar = true }) { Icon(Icons.Default.Delete, "Limpar lista") }
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { bipar() }, Modifier.fillMaxWidth().height(60.dp)) {
                            ConteudoBotao(IconesMapa.Bipar, "Bipar produtos com preço novo", MaterialTheme.typography.titleMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(checked = emSequencia, onCheckedChange = { emSequencia = it })
                            Spacer(Modifier.width(8.dp))
                            Text("Bipar um atrás do outro")
                        }
                        ultimo?.let { (texto, alerta) -> MensagemBipe(texto, alerta) }
                    }
                }
            }

            if (lista.isEmpty()) {
                item {
                    Text(
                        "Nenhuma etiqueta para trocar. Bipe os produtos que mudaram de preço " +
                            "e o app monta a lista na ordem do caminho pela loja.",
                        Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                item {
                    Text(
                        if (lista.size == 1) "1 etiqueta para trocar" else "${lista.size} etiquetas para trocar",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            lista.groupBy { it.secaoNome to it.secaoLocalizacao }.forEach { (secao, itens) ->
                val (nome, localizacao) = secao
                item(key = "secao-$nome-$localizacao") {
                    Column(Modifier.padding(top = 12.dp)) {
                        Text(
                            nome ?: "Sem local definido",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        if (!localizacao.isNullOrBlank()) {
                            Text(localizacao, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                items(itens, key = { it.produto.id }) { item ->
                    val p = item.produto
                    ListItem(
                        modifier = Modifier.clickable { nav.navigate(Rotas.produto(p.id)) },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        leadingContent = {
                            Checkbox(checked = false, onCheckedChange = {
                                vibrar.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch { db.produtos().marcarEtiqueta(p.id, false) }
                            })
                        },
                        headlineContent = { Text(p.titulo()) },
                        trailingContent = p.fotoPath?.let { foto -> { Miniatura(Fotos.arquivo(ctx, foto), 44.dp) } },
                        supportingContent = {
                            Text(listOfNotNull(p.prateleira?.let { "Prateleira $it" }, p.codigoBarras).joinToString(" • "))
                        },
                    )
                }
            }
        }
    }

    if (confirmarLimpar) {
        AlertDialog(
            onDismissRequest = { confirmarLimpar = false },
            title = { Text("Limpar a lista?") },
            text = { Text("Todos os produtos saem da lista de etiquetas. Eles continuam mapeados.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarLimpar = false
                    scope.launch { db.produtos().limparEtiquetas() }
                }) { Text("Limpar") }
            },
            dismissButton = { TextButton(onClick = { confirmarLimpar = false }) { Text("Cancelar") } },
        )
    }
}
