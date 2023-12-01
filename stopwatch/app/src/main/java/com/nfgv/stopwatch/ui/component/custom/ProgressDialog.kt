package com.nfgv.stopwatch.ui.component.custom

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.nfgv.stopwatch.R

class ProgressDialog(private val context: Context) {
    private var progressDialog: AlertDialog? = null

    private fun build() {
        val builder = AlertDialog.Builder(context).apply {
            setView(R.layout.progress_dialog)
            setCancelable(false)
        }

        progressDialog = builder.create()
    }

    fun show() {
        if (progressDialog == null) {
            build()
        }

        progressDialog?.show()
    }

    fun dismiss() {
        progressDialog?.dismiss()
    }
}