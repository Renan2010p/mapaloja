// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.core.ui

import android.content.Context
import android.widget.Toast

fun Context.aviso(mensagem: String) = Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
