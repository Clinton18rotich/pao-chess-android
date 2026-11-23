package com.example.paochess.data

import android.content.Context
import java.io.File

object CrashLogger {
    fun getLogs(context: Context): String {
        try {
            val process = Runtime.getRuntime().exec("logcat -d -t 500")
            val reader = process.inputStream.bufferedReader()
            val logs = reader.readText()
            // Filter for fatal exceptions
            val lines = logs.split("\n")
            val fatalIndex = lines.indexOfFirst { it.contains("FATAL EXCEPTION") }
            if (fatalIndex != -1) {
                return lines.subList(fatalIndex, minOf(fatalIndex + 25, lines.size)).joinToString("\n")
            }
            return "No crash found in recent logs. (App may have crashed before Logcat was set up)"
        } catch (e: Exception) {
            return "Error reading logs: ${e.message}"
        }
    }
}
