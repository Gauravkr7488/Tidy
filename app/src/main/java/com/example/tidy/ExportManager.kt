/*
 * Copyright (C) 2026  Gaurav Kumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.example.tidy

import android.content.Context
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.example.tidy.Utils.createBackupJson
import com.example.tidy.Utils.getCurrentDate
import com.yourapp.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportManager(
    private val context: Context,
    private val database: AppDatabase
) {
    private val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)

    suspend fun exportSilently(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            // TODO FIX
            val dbOperation = DbOperation(database)
            val tasks = dbOperation.taskGetAll()
            var lastResetDate = dbOperation.getLastResetDate()
            if (lastResetDate == null) lastResetDate = getCurrentDate()
            val json = createBackupJson(tasks, lastResetDate)

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "backup_$timestamp.json"

            val savedUri = prefs.getString("backup_uri", null)

            if (savedUri != null) {
                // Write to user-picked folder
                val treeUri = savedUri.toUri()
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