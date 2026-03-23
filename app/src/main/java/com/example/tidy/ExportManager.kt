package com.example.tidy

import android.content.Context
import com.google.gson.Gson
import io.objectbox.Box
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportManager(
    private val context: Context,
    private val taskBox: Box<Task>
) {
    suspend fun exportSilently(): Result<File> = withContext(Dispatchers.IO) {
        return@withContext try {
            val json = Gson().toJson(taskBox.all.map { it.toDto() })

            val backupDir = File(context.filesDir, "backups").also { it.mkdirs() }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(backupDir, "backup_$timestamp.json")

            file.writeText(json)
            Result.success(file)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}