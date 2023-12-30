package com.nfgv.stopwatch.ui.binding

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.nfgv.stopwatch.R

object BindingAdapters {
    @BindingAdapter("app:textBackupPresent")
    @JvmStatic
    fun setBackupPresentText(textView: TextView, isPresent: Boolean) {
        val resId = if (isPresent) R.string.backup_present else R.string.backup_not_present
        textView.setText(resId)
    }

    @BindingAdapter("app:textSheetsConnected")
    @JvmStatic
    fun setSheetsConnectedText(textView: TextView, isConnected: Boolean) {
        val resId = if (isConnected) R.string.sheets_connected else R.string.sheets_disconnected
        textView.setText(resId)
    }

    @BindingAdapter("app:textDataSource")
    @JvmStatic
    fun setDataSourceText(textView: TextView, isConnected: Boolean) {
        val resId = if (isConnected) R.string.source_sheets else R.string.source_backup
        textView.setText(resId)
    }
}