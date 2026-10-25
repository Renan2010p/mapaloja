// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.feature.inicio

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.renan.mapaloja.core.data.preferencias.Preferencias
import com.renan.mapaloja.core.ui.icones.IconesMapa
import com.renan.mapaloja.core.ui.navegacao.Rotas
import kotlinx.coroutines.launch

private data class Pagina(val icone: ImageVector, val titulo: String, val texto: String)

private val paginas = listOf(
    Pagina(
        IconesMapa.Local,
        "Bem-vindo ao Mapa da Loja",
        "Encontre qualquer produto em segundos: bipe o código de barras e veja a seção, " +
            "a prateleira e o ponto exato na foto da gôndola.",
    ),
    Pagina(
        IconesMapa.Estante,
        "Mapeie a loja uma vez",
        "Crie uma seção para cada gôndola e tire uma foto. Depois escolha a prateleira " +
            "e vá bipando os produtos, um atrás do outro.",
    ),
    Pagina(
        IconesMapa.Etiqueta,
        "Troque etiquetas sem rodar a loja",
        "Bipe os produtos que mudaram de preço e o app monta a lista na ordem do caminho " +
            "pela loja. É só seguir e ir marcando.",
    ),
)

@Composable
internal fun BoasVindasScreen(nav: NavController) {
    val ctx = LocalContext.current
    val preferencias = remember { Preferencias.get(ctx) }
    val scope = rememberCoroutineScope()
    val total = paginas.size + 1 // + página de configuração
    val pager = rememberPagerState(pageCount = { total })
    var nomeLoja by rememberSaveable { mutableStateOf(preferencias.nomeLoja.value) }
    val ultima = pager.currentPage == total - 1

    fun concluir() {
        preferencias.concluirBoasVindas(nomeLoja)
        nav.navigate(Rotas.BUSCA) { popUpTo(nav.graph.id) { inclusive = true } }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .imePadding()
            .padding(24.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.CenterEnd) {
            if (!ultima) TextButton(onClick = { scope.launch { pager.animateScrollToPage(total - 1) } }) { Text("Pular") }
        }

        HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { i ->
            if (i < paginas.size) {
                PaginaApresentacao(paginas[i], mostrarCreditos = i == 0)
            } else {
                PaginaConfiguracao(nomeLoja) { nomeLoja = it }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(total) { i -> Indicador(ativo = i == pager.currentPage) }
        }

        Button(
            onClick = { if (ultima) concluir() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Text(if (ultima) "Começar" else "Próximo", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun PaginaApresentacao(pagina: Pagina, mostrarCreditos: Boolean) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { Icon(pagina.icone, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.height(32.dp))
        Text(
            pagina.titulo,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            pagina.texto,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (mostrarCreditos) {
            Spacer(Modifier.height(32.dp))
            Text(
                "Feito pelo Claude e Renan Lucas Vieira Hilário",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PaginaConfiguracao(nomeLoja: String, onNomeLoja: (String) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Vamos configurar", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Dê um nome para identificar a loja. Ele aparece no topo da tela inicial.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = nomeLoja,
            onValueChange = onNomeLoja,
            label = { Text("Nome da loja (opcional)") },
            placeholder = { Text("Ex: Loja Centro") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))
        Dica(IconesMapa.Celular, "Tudo fica salvo só neste celular, sem internet e sem conta.")
        Dica(IconesMapa.Backup, "Faça backup de vez em quando pelo menu ⋮ da tela inicial.")
        Dica(IconesMapa.Codigo, "Software livre (GPL-3.0): o código é aberto para todos.")
    }
}

@Composable
private fun Dica(icone: ImageVector, texto: String) {
    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icone, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun Indicador(ativo: Boolean) {
    val largura by animateDpAsState(if (ativo) 24.dp else 8.dp, label = "largura")
    val cor by animateColorAsState(
        if (ativo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        label = "cor",
    )
    Box(
        Modifier
            .padding(horizontal = 4.dp)
            .height(8.dp)
            .width(largura)
            .clip(CircleShape)
            .background(cor),
    )
}
