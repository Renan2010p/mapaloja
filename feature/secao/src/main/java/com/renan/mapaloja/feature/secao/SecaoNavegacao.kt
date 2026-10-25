// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.feature.secao

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.renan.mapaloja.core.ui.navegacao.Rotas

/** Lista de seções, tela da seção (modo mapeamento) e edição de seção. */
fun NavGraphBuilder.telasDeSecao(nav: NavController) {
    val id = listOf(navArgument("id") { type = NavType.LongType })

    composable(Rotas.SECOES) { SecoesScreen(nav) }

    composable(Rotas.SECAO, arguments = id) {
        SecaoScreen(nav, it.arguments!!.getLong("id"))
    }

    composable(Rotas.SECAO_EDITAR, arguments = id) {
        SecaoEditarScreen(nav, it.arguments!!.getLong("id"))
    }
}
