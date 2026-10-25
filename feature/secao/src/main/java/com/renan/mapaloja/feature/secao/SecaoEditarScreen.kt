// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.feature.secao

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.renan.mapaloja.core.data.db.AppDatabase
import com.renan.mapaloja.core.data.fotos.Fotos
import com.renan.mapaloja.core.data.model.Secao
import com.renan.mapaloja.core.ui.componentes.FotoComMarcadores
import com.renan.mapaloja.core.ui.componentes.TelaBase
import com.renan.mapaloja.core.ui.navegacao.Rotas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
    var fotoTirando by rememberSaveable { mutableStateOf<String?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }
    var confirmarExclusao by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        if (carregado) return@LaunchedEffect
        db.secoes().porId(id)?.let {
            nome = it.nome
            localizacao = it.localizacao
            foto = it.fotoPath
            fotoOriginal = it.fotoPath
        }
        carregado = true
    }

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val arquivo = fotoTirando?.let { Fotos.arquivo(ctx, it) } ?: return@rememberLauncherForActivityResult
        fotoTirando = null
        if (ok && arquivo.length() > 0) {
            scope.launch {
                withContext(Dispatchers.IO) { Fotos.reduzir(arquivo) }
                // Uma foto nova tirada antes nesta mesma tela e não salva pode ir embora
                foto?.takeIf { it != fotoOriginal }?.let { Fotos.arquivo(ctx, it).delete() }
                foto = arquivo.name
            }
        } else {
            arquivo.delete()
        }
    }

    fun tirarFoto() {
        val arquivo: File = Fotos.novoArquivo(ctx)
        fotoTirando = arquivo.name
        camera.launch(Fotos.uri(ctx, arquivo))
    }

    fun salvar() = scope.launch {
        if (nome.isBlank()) {
            erro = "Dê um nome para a seção."
            return@launch
        }
        val secao = Secao(id = id, nome = nome.trim(), localizacao = localizacao.trim(), fotoPath = foto)
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
            OutlinedButton(onClick = { tirarFoto() }, Modifier.fillMaxWidth().height(56.dp)) {
                Text(if (foto == null) "📷  Tirar foto da seção" else "📷  Tirar outra foto")
            }
            Text(
                "Dica: fotografe a gôndola inteira de frente, com boa luz, pra dar pra ver as prateleiras.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

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
