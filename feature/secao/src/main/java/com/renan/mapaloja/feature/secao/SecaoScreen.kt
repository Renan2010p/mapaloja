// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.feature.secao

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.renan.mapaloja.core.ui.componentes.FotoComMarcadores
import com.renan.mapaloja.core.ui.componentes.Marcador
import com.renan.mapaloja.core.ui.componentes.MensagemBipe
import com.renan.mapaloja.core.ui.componentes.Miniatura
import com.renan.mapaloja.core.ui.componentes.SeletorPrateleira
import com.renan.mapaloja.core.ui.componentes.TelaBase
import com.renan.mapaloja.core.ui.icones.IconesMapa
import com.renan.mapaloja.core.ui.leitor.lerCodigo
import com.renan.mapaloja.core.ui.navegacao.Rotas
import kotlinx.coroutines.launch

/** Tela da seção + modo mapeamento. */
@Composable
internal fun SecaoScreen(nav: NavController, id: Long) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    val scope = rememberCoroutineScope()
    val vibrar = LocalHapticFeedback.current
    val secao by remember { db.secoes().observar(id) }.collectAsStateWithLifecycle(null)
    val produtos by remember { db.produtos().daSecao(id) }.collectAsStateWithLifecycle(emptyList())

    var prateleira by rememberSaveable { mutableStateOf(1) }
    var emSequencia by rememberSaveable { mutableStateOf(true) }
    var ultimo by remember { mutableStateOf<String?>(null) }
    var codigoNovo by remember { mutableStateOf<String?>(null) }
    var mover by remember { mutableStateOf<Pair<Produto, String>?>(null) }

    fun colocarAqui(p: Produto): Produto {
        // Se mudou de seção ou de prateleira, a marcação antiga na foto não vale mais
        val mesmoLugar = p.secaoId == id && p.prateleira == prateleira
        return p.copy(
            secaoId = id,
            prateleira = prateleira,
            posX = if (mesmoLugar) p.posX else null,
            posY = if (mesmoLugar) p.posY else null,
            atualizadoEm = System.currentTimeMillis(),
        )
    }

    fun bipar() {
        lerCodigo(ctx) { lido ->
            scope.launch {
                val ean = normalizarCodigo(lido)
                val existente = db.produtos().porEan(ean)
                val secaoAtual = existente?.secaoId
                when {
                    existente == null -> codigoNovo = ean
                    secaoAtual != null && secaoAtual != id -> {
                        val outra = db.secoes().porId(secaoAtual)?.nome ?: "outra seção"
                        mover = existente to outra
                    }
                    else -> {
                        db.produtos().atualizar(colocarAqui(existente))
                        vibrar.performHapticFeedback(HapticFeedbackType.LongPress)
                        ultimo = "${existente.titulo()} → prateleira $prateleira"
                        if (emSequencia) bipar()
                    }
                }
            }
        }
    }

    TelaBase(
        titulo = secao?.nome ?: "",
        onVoltar = { nav.popBackStack() },
        acoes = {
            IconButton(onClick = { nav.navigate(Rotas.secaoEditar(id)) }) { Icon(Icons.Default.Edit, "Editar seção") }
        },
    ) { padding ->
        val s = secao ?: return@TelaBase
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 16.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                FotoComMarcadores(
                    arquivo = s.fotoPath?.let { Fotos.arquivo(ctx, it) },
                    marcadores = produtos.mapNotNull { Marcador.de(it.posX, it.posY, destaque = false) },
                )
            }
            if (s.localizacao.isNotBlank()) item { Text(s.localizacao, style = MaterialTheme.typography.titleMedium) }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Mapear produtos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Prateleira atual (1 = a mais alta):")
                        SeletorPrateleira(
                            valor = prateleira,
                            onMudar = { if (it != null) prateleira = it },
                            quantidade = s.prateleiras,
                            onAdicionar = {
                                val nova = s.prateleiras + 1
                                scope.launch { db.secoes().atualizar(s.copy(prateleiras = nova)) }
                                prateleira = nova
                            },
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(checked = emSequencia, onCheckedChange = { emSequencia = it })
                            Spacer(Modifier.width(8.dp))
                            Text("Bipar um atrás do outro")
                        }
                        Button(onClick = { bipar() }, Modifier.fillMaxWidth().height(64.dp)) {
                            ConteudoBotao(IconesMapa.Bipar, "Bipar na prateleira $prateleira", MaterialTheme.typography.titleMedium)
                        }
                        TextButton(onClick = { nav.navigate(Rotas.produtoEditar(secao = id)) }) {
                            Text("Adicionar produto sem código de barras")
                        }
                        ultimo?.let { MensagemBipe(it) }
                    }
                }
            }

            if (produtos.isEmpty()) {
                item { Text("Nenhum produto nesta seção ainda.") }
            }
            produtos.groupBy { it.prateleira }.forEach { (n, lista) ->
                item(key = "prateleira-$n") {
                    Text(
                        if (n == null) "Sem prateleira" else "Prateleira $n",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                items(lista, key = { it.id }) { p ->
                    ListItem(
                        modifier = Modifier.clickable { nav.navigate(Rotas.produtoEditar(p.id)) },
                        leadingContent = p.fotoPath?.let { foto -> { Miniatura(Fotos.arquivo(ctx, foto), 48.dp) } },
                        headlineContent = { Text(p.titulo()) },
                        supportingContent = {
                            Text(
                                listOfNotNull(
                                    p.codigoBarras,
                                    p.codigoInterno,
                                    if (p.posX == null) "sem marcação na foto" else null,
                                ).joinToString(" • ")
                            )
                        },
                    )
                }
            }
        }
    }

    codigoNovo?.let { codigo ->
        var nome by remember(codigo) { mutableStateOf("") }
        var interno by remember(codigo) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { codigoNovo = null },
            title = { Text("Produto novo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Código $codigo → prateleira $prateleira")
                    OutlinedTextField(nome, { nome = it }, label = { Text("Nome (opcional)") })
                    OutlinedTextField(interno, { interno = it }, label = { Text("Código interno (opcional)") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    codigoNovo = null
                    scope.launch {
                        val novo = Produto(
                            codigoBarras = codigo,
                            codigoInterno = interno.trim().ifEmpty { null },
                            nome = nome.trim(),
                            secaoId = id,
                            prateleira = prateleira,
                        )
                        db.produtos().inserir(novo)
                        vibrar.performHapticFeedback(HapticFeedbackType.LongPress)
                        ultimo = "${novo.titulo()} cadastrado na prateleira $prateleira"
                        if (emSequencia) bipar()
                    }
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton(onClick = { codigoNovo = null }) { Text("Cancelar") } },
        )
    }

    mover?.let { (p, outraSecao) ->
        AlertDialog(
            onDismissRequest = { mover = null },
            title = { Text("Produto já mapeado") },
            text = { Text("\"${p.titulo()}\" está em $outraSecao. Mudar para cá (prateleira $prateleira)?") },
            confirmButton = {
                TextButton(onClick = {
                    mover = null
                    scope.launch {
                        db.produtos().atualizar(colocarAqui(p))
                        vibrar.performHapticFeedback(HapticFeedbackType.LongPress)
                        ultimo = "${p.titulo()} mudou para prateleira $prateleira"
                        if (emSequencia) bipar()
                    }
                }) { Text("Mudar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    mover = null
                    if (emSequencia) bipar()
                }) { Text("Deixar onde está") }
            },
        )
    }
}
