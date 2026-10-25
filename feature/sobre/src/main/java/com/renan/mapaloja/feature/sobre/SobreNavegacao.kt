// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.feature.sobre

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.renan.mapaloja.core.ui.navegacao.Rotas

/** Sobre o app, créditos e licença. */
fun NavGraphBuilder.telasDeSobre(nav: NavController) {
    composable(Rotas.SOBRE) { SobreScreen(nav) }
}
