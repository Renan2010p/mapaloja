// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.feature.busca

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.renan.mapaloja.core.ui.navegacao.Rotas

/** Tela inicial: bipar e buscar produtos. */
fun NavGraphBuilder.telasDeBusca(nav: NavController) {
    composable(Rotas.BUSCA) { BuscaScreen(nav) }
}
