package com.colcastar.web

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.Exception

internal class Logger {
    fun log(context: Context, e: Exception) {
        val sw = StringWriter()
        e.printStackTrace(PrintWriter(sw))
        val stacktrace: String = sw.toString()
        val file = File(context.getExternalFilesDir(null), "log.txt")
        file.createNewFile()
        file.writeText(stacktrace)
    }
}