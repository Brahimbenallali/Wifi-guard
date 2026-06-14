package com.wifiguard.app.report

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseBackupManager(
    private val context: Context,
    private val closeDatabase: () -> Unit
) {
    private val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    fun backup(): File {
        val source = context.getDatabasePath("wifi_guard.db")
        val destination = File(backupDir(), "wifi_guard_${timestamp.format(Date())}.db")
        source.copyTo(destination, overwrite = true)
        return destination
    }

    fun restoreLatest(): File? {
        val latest = backupDir().listFiles { file -> file.name.startsWith("wifi_guard_") && file.extension == "db" }
            ?.maxByOrNull { it.lastModified() }
            ?: return null
        closeDatabase()
        latest.copyTo(context.getDatabasePath("wifi_guard.db"), overwrite = true)
        return latest
    }

    private fun backupDir(): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        return File(dir, "backups").apply { mkdirs() }
    }
}
