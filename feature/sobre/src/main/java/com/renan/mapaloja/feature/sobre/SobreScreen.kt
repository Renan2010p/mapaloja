// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.feature.sobre

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.renan.mapaloja.core.ui.componentes.TelaBase
import com.renan.mapaloja.core.ui.icones.IconesMapa
import com.renan.mapaloja.core.ui.navegacao.Rotas

private const val CODIGO_FONTE = "https://github.com/Renan2010p/mapaloja"
private const val LICENCA = "https://www.gnu.org/licenses/gpl-3.0.html"

private val bibliotecas = listOf(
    "Kotlin" to "Apache 2.0",
    "Jetpack Compose e Material 3" to "Apache 2.0",
    "Room" to "Apache 2.0",
    "Navigation Compose" to "Apache 2.0",
    "Google Code Scanner (ML Kit)" to "Termos de serviço do Google",
)

@Composable
internal fun SobreScreen(nav: NavController) {
    val ctx = LocalContext.current
    val versao = remember {
        runCatching { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName }.getOrNull() ?: "?"
    }
    fun abrir(url: String) = ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    TelaBase(titulo = "Sobre", onVoltar = { nav.popBackStack() }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) { Icon(IconesMapa.Local, null, Modifier.size(52.dp), tint = MaterialTheme.colorScheme.onPrimary) }
            Text("Mapa da Loja", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Versão $versao", color = MaterialTheme.colorScheme.onSurfaceVariant)

            Secao("Créditos") {
                Text(
                    "Feito pelo Claude e Renan Lucas Vieira Hilário",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Secao("Licença") {
                Text(
                    "Software livre sob a licença GNU GPL versão 3 ou posterior. Você pode usar, " +
                        "estudar, modificar e compartilhar, desde que mantenha a mesma licença.",
                )
                Text(
                    "Distribuído sem nenhuma garantia.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { abrir(CODIGO_FONTE) }, Modifier.weight(1f)) { Text("Código-fonte") }
                    OutlinedButton(onClick = { abrir(LICENCA) }, Modifier.weight(1f)) { Text("Ler a licença") }
                }
            }

            Secao("Feito com") {
                bibliotecas.forEach { (nome, licenca) ->
                    Row(Modifier.fillMaxWidth()) {
                        Text(nome, Modifier.weight(1f))
                        Text(licenca, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            OutlinedButton(onClick = { nav.navigate(Rotas.BOAS_VINDAS) }, Modifier.fillMaxWidth()) {
                Text("Ver boas-vindas de novo")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Secao(titulo: String, conteudo: @Composable () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            conteudo()
        }
    }
}
