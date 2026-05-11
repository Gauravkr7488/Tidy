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
import androidx.documentfile.provider.DocumentFile
import io.objectbox.Box
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri
import com.example.tidy.Utils.createBackupJson
import com.example.tidy.Utils.getCurrentDate

class ExportManager(
    private val context: Context,
    private val taskBox: Box<Task>,
    private val lastBoxReset: Box<LastReset>
) {
    private val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)

    suspend fun exportSilently(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            // TODO FIX
            val tasks = taskBox.all
            var lastResetDate = lastBoxReset.get(1).lastResetDate
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