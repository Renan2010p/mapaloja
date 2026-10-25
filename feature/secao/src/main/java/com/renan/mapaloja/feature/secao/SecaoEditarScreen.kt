// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.feature.secao

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.renan.mapaloja.core.data.db.AppDatabase
import com.renan.mapaloja.core.data.fotos.Fotos
import com.renan.mapaloja.core.data.model.Secao
import com.renan.mapaloja.core.ui.componentes.BotoesFoto
import com.renan.mapaloja.core.ui.componentes.FotoComMarcadores
import com.renan.mapaloja.core.ui.componentes.TelaBase
import com.renan.mapaloja.core.ui.navegacao.Rotas
import kotlinx.coroutines.launch

@Composable
internal fun SecaoEditarScreen(nav: NavController, id: Long) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    val scope = rememberCoroutineScope()

    var carregado by rememberSaveable { mutableStateOf(id == 0L) }
    var nome by rememberSaveable { mutableStateOf("") }
    var localizacao by rememberSaveable { mutableStateOf("") }
    var foto by rememberSaveable { mutableStateOf<String?>(null) }
    var fotoOriginal by rememberSaveable { mutableStateOf<String?>(null) }
    var prateleiras by rememberSaveable { mutableStateOf(5) }
    var erro by remember { mutableStateOf<String?>(null) }
    var confirmarExclusao by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        if (carregado) return@LaunchedEffect
        db.secoes().porId(id)?.let {
            nome = it.nome
            localizacao = it.localizacao
            foto = it.fotoPath
            fotoOriginal = it.fotoPath
            prateleiras = it.prateleiras
        }
        carregado = true
    }

    fun novaFoto(nome: String) {
        // Uma foto nova escolhida antes nesta mesma tela e não salva pode ir embora
        foto?.takeIf { it != fotoOriginal }?.let { Fotos.arquivo(ctx, it).delete() }
        foto = nome
    }

    fun salvar() = scope.launch {
        if (nome.isBlank()) {
            erro = "Dê um nome para a seção."
            return@launch
        }
        val base = db.secoes().porId(id) ?: Secao(nome = "")
        val secao = base.copy(nome = nome.trim(), localizacao = localizacao.trim(), fotoPath = foto, prateleiras = prateleiras)
        if (fotoOriginal != null && fotoOriginal != foto) Fotos.arquivo(ctx, fotoOriginal!!).delete()
        if (id == 0L) {
            val novoId = db.secoes().inserir(secao)
            nav.navigate(Rotas.secao(novoId)) { popUpTo(Rotas.SECAO_EDITAR) { inclusive = true } }
        } else {
            db.secoes().atualizar(secao)
            nav.popBackStack()
        }
    }

    TelaBase(
        titulo = if (id == 0L) "Nova seção" else "Editar seção",
        onVoltar = { nav.popBackStack() },
        acoes = {
            if (id != 0L) IconButton(onClick = { confirmarExclusao = true }) { Icon(Icons.Default.Delete, "Excluir") }
        },
    ) { padding ->
        if (!carregado) return@TelaBase
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome (ex: Vitaminas, Dermocosméticos)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = localizacao,
                onValueChange = { localizacao = it },
                label = { Text("Onde fica (ex: Corredor 3, lado esquerdo)") },
                modifier = Modifier.fillMaxWidth(),
            )

            FotoComMarcadores(arquivo = foto?.let { Fotos.arquivo(ctx, it) }, marcadores = emptyList())
            BotoesFoto(temFoto = foto != null, onFoto = ::novaFoto)
            Text(
                "Dica: fotografe a gôndola inteira de frente, com boa luz, pra dar pra ver as prateleiras.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Prateleiras na gôndola", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { if (prateleiras > 1) prateleiras-- }) { Text("−", style = MaterialTheme.typography.headlineSmall) }
                Text("$prateleiras", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = { if (prateleiras < 30) prateleiras++ }) { Icon(Icons.Default.Add, "Mais uma prateleira") }
            }

            erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = { salvar() }, Modifier.fillMaxWidth().height(56.dp)) { Text("Salvar") }
        }
    }

    if (confirmarExclusao) {
        AlertDialog(
            onDismissRequest = { confirmarExclusao = false },
            title = { Text("Excluir seção?") },
            text = { Text("Os produtos dela continuam cadastrados, mas ficam sem local.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarExclusao = false
                    scope.launch {
                        db.secoes().porId(id)?.let {
                            db.secoes().excluir(it)
                            it.fotoPath?.let { f -> Fotos.arquivo(ctx, f).delete() }
                        }
                        nav.popBackStack(Rotas.SECOES, inclusive = false)
                    }
                }) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = { confirmarExclusao = false }) { Text("Cancelar") } },
        )
    }
}
