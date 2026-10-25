// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.feature.inicio

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.renan.mapaloja.core.ui.navegacao.Rotas

/** Boas-vindas e configuração inicial (aparece na primeira vez que o app abre). */
fun NavGraphBuilder.telasDeInicio(nav: NavController) {
    composable(Rotas.BOAS_VINDAS) { BoasVindasScreen(nav) }
}
