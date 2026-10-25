// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.feature.etiquetas

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.renan.mapaloja.core.ui.navegacao.Rotas

/** Lista de produtos que precisam de etiqueta nova. */
fun NavGraphBuilder.telasDeEtiquetas(nav: NavController) {
    composable(Rotas.ETIQUETAS) { EtiquetasScreen(nav) }
}
