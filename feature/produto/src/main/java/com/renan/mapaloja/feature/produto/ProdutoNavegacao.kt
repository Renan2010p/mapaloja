// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.feature.produto

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.renan.mapaloja.core.ui.navegacao.Rotas

/** "Onde está" e cadastro/edição de produto. */
fun NavGraphBuilder.telasDeProduto(nav: NavController) {
    composable(Rotas.PRODUTO, arguments = listOf(navArgument("id") { type = NavType.LongType })) {
        ProdutoDetalheScreen(nav, it.arguments!!.getLong("id"))
    }

    composable(
        Rotas.PRODUTO_EDITAR,
        arguments = listOf(
            navArgument("id") { type = NavType.LongType },
            navArgument("ean") { type = NavType.StringType; nullable = true; defaultValue = null },
            navArgument("secao") { type = NavType.LongType; defaultValue = 0L },
        ),
    ) {
        val args = it.arguments!!
        ProdutoEditarScreen(nav, args.getLong("id"), args.getString("ean"), args.getLong("secao"))
    }
}
