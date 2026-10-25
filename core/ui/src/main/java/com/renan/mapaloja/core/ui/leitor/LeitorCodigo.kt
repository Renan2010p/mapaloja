// SPDX-License-Identifier: GPL-3.0-or-later

package com.renan.mapaloja.core.ui.leitor

import android.content.Context
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.renan.mapaloja.core.ui.aviso

/** Abre o leitor de código de barras do Google Play Services. */
fun lerCodigo(context: Context, onLido: (String) -> Unit) {
    val opcoes = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_ITF,
            Barcode.FORMAT_QR_CODE,
        )
        .enableAutoZoom()
        .build()
    GmsBarcodeScanning.getClient(context, opcoes).startScan()
        .addOnSuccessListener { codigo -> codigo.rawValue?.let(onLido) }
        .addOnFailureListener { context.aviso("Não deu pra abrir o leitor: ${it.message}") }
}
