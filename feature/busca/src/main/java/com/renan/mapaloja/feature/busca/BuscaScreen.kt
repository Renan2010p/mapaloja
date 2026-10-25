// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.feature.busca

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.renan.mapaloja.core.data.backup.Backup
import com.renan.mapaloja.core.data.db.AppDatabase
import com.renan.mapaloja.core.data.fotos.Fotos
import com.renan.mapaloja.core.data.importacao.ImportarCsv
import com.renan.mapaloja.core.data.model.normalizarCodigo
import com.renan.mapaloja.core.data.model.textoLocal
import com.renan.mapaloja.core.data.preferencias.Preferencias
import com.renan.mapaloja.core.ui.aviso
import com.renan.mapaloja.core.ui.componentes.Miniatura
import com.renan.mapaloja.core.ui.componentes.TelaBase
import com.renan.mapaloja.core.ui.icones.IconesMapa
import com.renan.mapaloja.core.ui.leitor.lerCodigo
import com.renan.mapaloja.core.ui.navegacao.Rotas
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
internal fun BuscaScreen(nav: NavController) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    val scope = rememberCoroutineScope()

    var texto by rememberSaveable { mutableStateOf("") }
    var naoEncontrado by remember { mutableStateOf<String?>(null) }
    var menuAberto by remember { mutableStateOf(false) }
    var importarDe by remember { mutableStateOf<Uri?>(null) }

    val nomeLoja by remember { Preferencias.get(ctx).nomeLoja }.collectAsStateWithLifecycle()
    val total by db.produtos().contar().collectAsStateWithLifecycle(0)
    val etiquetas by remember { db.produtos().contarEtiquetas() }.collectAsStateWithLifecycle(0)
    val totalSecoes by remember { db.secoes().observarTodas().map { it.size } }.collectAsStateWithLifecycle(0)
    val resultados by remember(texto) {
        val t = texto.trim()
        if (t.isEmpty()) flowOf(emptyList()) else db.produtos().buscar(normalizarCodigo(t), t, t)
    }.collectAsStateWithLifecycle(emptyList())

    val exportar = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) scope.launch {
            runCatching { Backup.exportar(ctx, uri) }
                .onSuccess { ctx.aviso("Backup salvo!") }
                .onFailure { ctx.aviso("Erro no backup: ${it.message}") }
        }
    }
    val importar = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        importarDe = uri
    }
    val importarPlanilha = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            runCatching { ImportarCsv.importar(ctx, uri) }
                .onSuccess { ctx.aviso("Planilha: ${it.novos} novos, ${it.atualizados} atualizados, ${it.ignorados} ignorados") }
                .onFailure { ctx.aviso("Erro ao ler a planilha: ${it.message}") }
        }
    }

    fun bipar() = lerCodigo(ctx) { lido ->
        scope.launch {
            val produto = db.produtos().porEan(normalizarCodigo(lido))
                ?: db.produtos().porCodigoInterno(lido.trim())
            if (produto != null) nav.navigate(Rotas.produto(produto.id))
            else naoEncontrado = normalizarCodigo(lido)
        }
    }

    TelaBase(
        titulo = nomeLoja.ifBlank { "Mapa da Loja" },
        acoes = {
            IconButton(onClick = { menuAberto = true }) { Icon(Icons.Default.MoreVert, "Menu") }
            DropdownMenu(expanded = menuAberto, onDismissRequest = { menuAberto = false }) {
                DropdownMenuItem(text = { Text("Salvar backup") }, onClick = {
                    menuAberto = false
                    exportar.launch("mapa-da-loja-${LocalDate.now()}.zip")
                })
                DropdownMenuItem(text = { Text("Restaurar backup") }, onClick = {
                    menuAberto = false
                    importar.launch(arrayOf("application/zip", "application/octet-stream"))
                })
                DropdownMenuItem(text = { Text("Importar planilha (CSV)") }, onClick = {
                    menuAberto = false
                    importarPlanilha.launch(arrayOf("text/*", "application/vnd.ms-excel", "application/octet-stream"))
                })
                HorizontalDivider()
                DropdownMenuItem(text = { Text("Sobre e créditos") }, onClick = {
                    menuAberto = false
                    nav.navigate(Rotas.SOBRE)
                })
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(
                onClick = { bipar() },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Row(
                    Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(IconesMapa.Bipar, null, Modifier.size(44.dp))
                    Spacer(Modifier.width(18.dp))
                    Column {
                        Text("Bipar produto", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Aponte a câmera para o código de barras", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            TextField(
                value = texto,
                onValueChange = { texto = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = CircleShape,
                placeholder = { Text("Buscar por código ou nome") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (texto.isNotEmpty()) IconButton(onClick = { texto = "" }) { Icon(Icons.Default.Clear, "Limpar") }
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )

            if (texto.isBlank()) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Numero("Produtos", total, Modifier.weight(1f))
                    Numero("Seções", totalSecoes, Modifier.weight(1f))
                }
                Atalho(
                    IconesMapa.Etiqueta,
                    "Trocar etiquetas",
                    if (etiquetas == 0) "Bipe os produtos que mudaram de preço" else "$etiquetas na lista, em ordem pela loja",
                ) { nav.navigate(Rotas.ETIQUETAS) }
                Atalho(IconesMapa.Estante, "Seções da loja", "Fotos das gôndolas e modo mapeamento") { nav.navigate(Rotas.SECOES) }
                Atalho(IconesMapa.NovoProduto, "Novo produto", "Cadastrar sem bipar") { nav.navigate(Rotas.produtoEditar()) }
            } else if (resultados.isEmpty()) {
                Text("Nada encontrado para \"${texto.trim()}\".")
                OutlinedButton(onClick = {
                    val t = texto.trim()
                    // Se digitou só números, provavelmente é o código de barras
                    nav.navigate(Rotas.produtoEditar(ean = if (t.all { it.isDigit() }) normalizarCodigo(t) else null))
                }) { Text("Cadastrar produto") }
            } else {
                LazyColumn {
                    items(resultados, key = { it.produto.id }) { r ->
                        ListItem(
                            modifier = Modifier.clickable { nav.navigate(Rotas.produto(r.produto.id)) },
                            leadingContent = r.produto.fotoPath?.let { foto -> { Miniatura(Fotos.arquivo(ctx, foto), 48.dp) } },
                            headlineContent = { Text(r.produto.titulo(), fontWeight = FontWeight.SemiBold) },
                            supportingContent = {
                                Text(textoLocal(r.secaoNome, r.secaoLocalizacao, r.produto.prateleira))
                            },
                            overlineContent = {
                                Text(listOfNotNull(r.produto.codigoBarras, r.produto.codigoInterno).joinToString(" • "))
                            },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    naoEncontrado?.let { codigo ->
        AlertDialog(
            onDismissRequest = { naoEncontrado = null },
            title = { Text("Produto não mapeado") },
            text = { Text("O código $codigo ainda não está cadastrado. Quer cadastrar agora?") },
            confirmButton = {
                TextButton(onClick = {
                    naoEncontrado = null
                    nav.navigate(Rotas.produtoEditar(ean = codigo))
                }) { Text("Cadastrar") }
            },
            dismissButton = { TextButton(onClick = { naoEncontrado = null }) { Text("Fechar") } },
        )
    }

    importarDe?.let { uri ->
        AlertDialog(
            onDismissRequest = { importarDe = null },
            title = { Text("Restaurar backup?") },
            text = { Text("Isso APAGA o mapa atual deste celular e coloca o do backup no lugar.") },
            confirmButton = {
                TextButton(onClick = {
                    importarDe = null
                    scope.launch {
                        runCatching { Backup.importar(ctx, uri) }
                            .onSuccess { (s, p) -> ctx.aviso("Restaurado: $s seções e $p produtos") }
                            .onFailure { ctx.aviso("Erro ao restaurar: ${it.message}") }
                    }
                }) { Text("Restaurar") }
            },
            dismissButton = { TextButton(onClick = { importarDe = null }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun Numero(rotulo: String, valor: Int, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp)) {
            Text("$valor", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(rotulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Atalho(icone: ImageVector, titulo: String, descricao: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) { Icon(icone, null, tint = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(descricao, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
