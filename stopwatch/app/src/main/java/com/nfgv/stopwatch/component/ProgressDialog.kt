package com.nfgv.stopwatch.component

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.nfgv.stopwatch.R

class ProgressDialog(private val context: Context) {
    private var progressDialog: AlertDialog? = null

    private fun build() {
        val builder = AlertDialog.Builder(context).also {
            it.setView(R.layout.progress_dialog)
            it.setCancelable(false)
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