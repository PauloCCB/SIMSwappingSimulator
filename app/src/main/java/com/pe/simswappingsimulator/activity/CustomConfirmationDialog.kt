package com.pe.simswappingsimulator.activity

import android.app.AlertDialog
import android.content.Context

class CustomConfirmationDialog(private val context: Context) {

    fun showConfirmationDialog(title: String, message: String, positiveText: String, negativeText: String, onPositiveClick: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)

        builder.setPositiveButton(positiveText) { dialog, _ ->
            onPositiveClick.invoke()
            dialog.dismiss()
        }

        builder.setNegativeButton(negativeText) { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    fun showConfirmationDialog(title: String, message: String, buttonText: String, onButtonClick: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)

        builder.setPositiveButton(buttonText) { dialog, _ ->
            onButtonClick.invoke()
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}
