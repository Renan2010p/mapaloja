// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.core.ui.componentes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.renan.mapaloja.core.data.fotos.Fotos
import com.renan.mapaloja.core.ui.aviso
import com.renan.mapaloja.core.ui.icones.IconesMapa
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Botões "Câmera" e "Galeria". Quando a foto chega, ela já foi reduzida e salva
 * na pasta do app, e [onFoto] recebe o nome do arquivo novo.
 */
@Composable
fun BotoesFoto(temFoto: Boolean, onFoto: (String) -> Unit, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    // Nome do arquivo que a câmera está preenchendo (sobrevive se o Android fechar o app no meio)
    var tirando by rememberSaveable { mutableStateOf<String?>(null) }

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val arquivo = tirando?.let { Fotos.arquivo(ctx, it) } ?: return@rememberLauncherForActivityResult
        tirando = null
        if (ok && arquivo.length() > 0) {
            scope.launch {
                withContext(Dispatchers.IO) { Fotos.reduzir(arquivo) }
                onFoto(arquivo.name)
            }
        } else {
            arquivo.delete()
        }
    }
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            val arquivo = withContext(Dispatchers.IO) { Fotos.importar(ctx, uri) }
            if (arquivo != null) onFoto(arquivo.name) else ctx.aviso("Não deu pra abrir essa imagem")
        }
    }

    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = {
                val arquivo = Fotos.novoArquivo(ctx)
                tirando = arquivo.name
                camera.launch(Fotos.uri(ctx, arquivo))
            },
            modifier = Modifier.weight(1f).height(52.dp),
        ) { ConteudoBotao(IconesMapa.Camera, if (temFoto) "Tirar outra" else "Câmera") }
        OutlinedButton(
            onClick = { galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            modifier = Modifier.weight(1f).height(52.dp),
        ) { ConteudoBotao(IconesMapa.Galeria, "Galeria") }
    }
}
