package com.example.tidy

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
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
    private val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)

    suspend fun exportSilently(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val json = Gson().toJson(taskBox.all.map { it.toDto() })
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "backup_$timestamp.json"

            val savedUri = prefs.getString("backup_uri", null)

            if (savedUri != null) {
                // Write to user-picked folder
                val treeUri = Uri.parse(savedUri)
                val docTree = DocumentFile.fromTreeUri(context, treeUri)
                val file = docTree?.createFile("application/json", fileName)
                file?.uri?.let { fileUri ->
                    context.contentResolver.openOutputStream(fileUri)?.use { stream ->
                        stream.write(json.toByteArray())
                    }
                }
            } else {
                // Fallback to internal storage if no folder picked yet
                val backupDir = File(context.filesDir, "backups").also { it.mkdirs() }
                File(backupDir, fileName).writeText(json)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}