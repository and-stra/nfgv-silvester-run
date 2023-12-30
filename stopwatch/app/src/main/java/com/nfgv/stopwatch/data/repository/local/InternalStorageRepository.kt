package com.nfgv.stopwatch.data.repository.local

import android.content.Context
import java.io.FileNotFoundException
import java.lang.StringBuilder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InternalStorageRepository @Inject constructor(private val context: Context) {
    fun writeToFile(fileName: String, content: String, creationMode: Int = Context.MODE_PRIVATE) {
        context.openFileOutput(fileName, creationMode).use {
            it.write(content.toByteArray())
        }
    }

    fun appendToFile(fileName: String, content: String) {
        writeToFile(fileName, content, Context.MODE_APPEND)
    }

    fun readFile(fileName: String): String? {
        return try {
            context.openFileInput(fileName).bufferedReader().useLines { lines ->
                lines.fold("") { accumulated, line ->
                    StringBuilder()
                        .append(accumulated)
                        .append("\n")
                        .append(line)
                        .toString()
                }
            }
        } catch (e: FileNotFoundException) {
            null
        }
    }

    fun deleteFile(fileName: String) {
        context.deleteFile(fileName)
    }
}