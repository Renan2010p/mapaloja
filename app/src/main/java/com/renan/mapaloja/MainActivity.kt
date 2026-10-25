// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.renan.mapaloja.core.data.preferencias.Preferencias
import com.renan.mapaloja.core.ui.navegacao.Rotas
import com.renan.mapaloja.core.ui.tema.TemaMapa
import com.renan.mapaloja.feature.busca.telasDeBusca
import com.renan.mapaloja.feature.etiquetas.telasDeEtiquetas
import com.renan.mapaloja.feature.inicio.telasDeInicio
import com.renan.mapaloja.feature.produto.telasDeProduto
import com.renan.mapaloja.feature.secao.telasDeSecao
import com.renan.mapaloja.feature.sobre.telasDeSobre

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent { TemaMapa { App() } }
    }
}

@Composable
fun App() {
    val ctx = LocalContext.current
    val nav = rememberNavController()
    val inicio = remember {
        if (Preferencias.get(ctx).boasVindasConcluida) Rotas.BUSCA else Rotas.BOAS_VINDAS
    }
    NavHost(nav, startDestination = inicio) {
        telasDeInicio(nav)
        telasDeBusca(nav)
        telasDeEtiquetas(nav)
        telasDeProduto(nav)
        telasDeSecao(nav)
        telasDeSobre(nav)
    }
}
