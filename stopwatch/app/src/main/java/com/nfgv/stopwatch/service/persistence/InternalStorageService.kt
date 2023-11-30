package com.nfgv.stopwatch.service.persistence

import android.content.Context
import java.lang.StringBuilder

class InternalStorageService {
    companion object {
        val instance: InternalStorageService by lazy {
            InternalStorageService()
        }
    }

    fun writeFile(
        context: Context,
        fileName: String,
        content: String,
        accessMode: Int = Context.MODE_PRIVATE
    ) {
        context.openFileOutput(fileName, accessMode).use {
            it.write(content.toByteArray())
        }
    }

    fun appendToFile(context: Context, fileName: String, content: String) {
        writeFile(context, fileName, content, Context.MODE_APPEND)
    }

    fun readFile(context: Context, fileName: String): String {
        return context.openFileInput(fileName).bufferedReader().useLines { lines ->
            lines.fold("") { some, text ->
                StringBuilder()
                    .append(some)
                    .append("\n")
                    .append(text)
                    .toString()
            }
        }
    }

    fun deleteFile(context: Context, fileName: String) {
        context.deleteFile(fileName)
    }

    fun fileExists(context: Context, fileName: String) {
        println(context.fileList())
    }
}