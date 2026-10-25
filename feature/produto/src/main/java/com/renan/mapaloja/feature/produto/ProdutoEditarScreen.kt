// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.feature.produto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.renan.mapaloja.core.data.db.AppDatabase
import com.renan.mapaloja.core.data.fotos.Fotos
import com.renan.mapaloja.core.data.model.Produto
import com.renan.mapaloja.core.data.model.normalizarCodigo
import com.renan.mapaloja.core.ui.aviso
import com.renan.mapaloja.core.ui.componentes.FotoComMarcadores
import com.renan.mapaloja.core.ui.componentes.Marcador
import com.renan.mapaloja.core.ui.componentes.SeletorPrateleira
import com.renan.mapaloja.core.ui.componentes.TelaBase
import com.renan.mapaloja.core.ui.leitor.lerCodigo
import com.renan.mapaloja.core.ui.navegacao.Rotas
import kotlinx.coroutines.launch

@Composable
internal fun ProdutoEditarScreen(nav: NavController, id: Long, eanInicial: String?, secaoInicial: Long) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    val scope = rememberCoroutineScope()
    val secoes by remember { db.secoes().observarTodas() }.collectAsStateWithLifecycle(emptyList())

    var carregado by rememberSaveable { mutableStateOf(id == 0L) }
    var ean by rememberSaveable { mutableStateOf(eanInicial ?: "") }
    var codigo by rememberSaveable { mutableStateOf("") }
    var nome by rememberSaveable { mutableStateOf("") }
    var secaoId by rememberSaveable { mutableStateOf(secaoInicial) }
    var prateleira by rememberSaveable { mutableStateOf<Int?>(null) }
    var posX by rememberSaveable { mutableStateOf<Float?>(null) }
    var posY by rememberSaveable { mutableStateOf<Float?>(null) }
    var obs by rememberSaveable { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }
    var confirmarExclusao by remember { mutableStateOf(false) }
    var menuSecao by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        if (carregado) return@LaunchedEffect
        db.produtos().porId(id)?.let { p ->
            ean = p.codigoBarras ?: ""
            codigo = p.codigoInterno ?: ""
            nome = p.nome
            secaoId = p.secaoId ?: 0L
            prateleira = p.prateleira
            posX = p.posX
            posY = p.posY
            obs = p.observacao
        }
        carregado = true
    }

    fun salvar() = scope.launch {
        val eanFinal = ean.trim().takeIf { it.isNotEmpty() }?.let(::normalizarCodigo)
        val codigoFinal = codigo.trim().takeIf { it.isNotEmpty() }
        if (eanFinal == null && codigoFinal == null && nome.isBlank()) {
            erro = "Preencha pelo menos o nome ou um código."
            return@launch
        }
        if (eanFinal != null) {
            val outro = db.produtos().porEan(eanFinal)
            if (outro != null && outro.id != id) {
                erro = "Esse código de barras já está cadastrado em \"${outro.titulo()}\"."
                return@launch
            }
        }
        val produto = Produto(
            id = id,
            codigoBarras = eanFinal,
            codigoInterno = codigoFinal,
            nome = nome.trim(),
            secaoId = secaoId.takeIf { it > 0 },
            prateleira = prateleira,
            posX = posX,
            posY = posY,
            observacao = obs.trim(),
        )
        if (id == 0L) db.produtos().inserir(produto) else db.produtos().atualizar(produto)
        ctx.aviso("Salvo!")
        nav.popBackStack()
    }

    TelaBase(
        titulo = if (id == 0L) "Novo produto" else "Editar produto",
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = ean,
                    onValueChange = { ean = it },
                    label = { Text("Código de barras (EAN)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                OutlinedButton(onClick = { lerCodigo(ctx) { ean = normalizarCodigo(it) } }, Modifier.height(64.dp)) {
                    Text("📷")
                }
            }
            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it },
                label = { Text("Código interno do produto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth(),
            )

            ExposedDropdownMenuBox(expanded = menuSecao, onExpandedChange = { menuSecao = it }) {
                OutlinedTextField(
                    value = secoes.firstOrNull { it.id == secaoId }?.nome ?: "Sem seção",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Seção") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(menuSecao) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                )
                ExposedDropdownMenu(expanded = menuSecao, onDismissRequest = { menuSecao = false }) {
                    DropdownMenuItem(text = { Text("Sem seção") }, onClick = {
                        secaoId = 0L; posX = null; posY = null; menuSecao = false
                    })
                    secoes.forEach { s ->
                        DropdownMenuItem(text = { Text(s.nome) }, onClick = {
                            if (s.id != secaoId) { posX = null; posY = null }
                            secaoId = s.id
                            menuSecao = false
                        })
                    }
                }
            }

            Text("Prateleira (1 = a mais alta)")
            SeletorPrateleira(prateleira, { prateleira = it }, permitirNenhuma = true)

            val secao = secoes.firstOrNull { it.id == secaoId }
            if (secao != null) {
                Text(
                    if (secao.fotoPath == null) "Essa seção ainda não tem foto."
                    else "Toque na foto pra marcar onde o produto fica:",
                )
                FotoComMarcadores(
                    arquivo = secao.fotoPath?.let { Fotos.arquivo(ctx, it) },
                    marcadores = listOfNotNull(Marcador.de(posX, posY)),
                    onToque = { x, y -> posX = x; posY = y },
                )
                if (posX != null) TextButton(onClick = { posX = null; posY = null }) { Text("Tirar marcação") }
            }

            OutlinedTextField(
                value = obs,
                onValueChange = { obs = it },
                label = { Text("Observação (opcional)") },
                modifier = Modifier.fillMaxWidth(),
            )

            erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(onClick = { salvar() }, Modifier.fillMaxWidth().height(56.dp)) { Text("Salvar") }
        }
    }

    if (confirmarExclusao) {
        AlertDialog(
            onDismissRequest = { confirmarExclusao = false },
            title = { Text("Excluir produto?") },
            text = { Text("Ele vai sair do mapa da loja.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarExclusao = false
                    scope.launch {
                        db.produtos().porId(id)?.let { db.produtos().excluir(it) }
                        nav.popBackStack()
                        // Se veio da tela "Onde está", ela também sai (o produto não existe mais)
                        if (nav.currentDestination?.route == Rotas.PRODUTO) nav.popBackStack()
                    }
                }) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = { confirmarExclusao = false }) { Text("Cancelar") } },
        )
    }
}
