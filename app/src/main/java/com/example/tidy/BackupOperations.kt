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
import android.net.Uri
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.example.tidy.Utils.createBackupJson
import com.example.tidy.Utils.toTask
import com.google.gson.Gson

class BackupOperations(
    private val dbOperation: DbOperation
) {
    suspend fun createBackup(
        context: Context,
        uri: Uri
    ) {
        try {
            val lastResetDate = dbOperation.getLastResetDate() ?: Utils.getCurrentDate()
            val json = createBackupJson(dbOperation.taskGetAll(), lastResetDate)

            context.contentResolver
                .openOutputStream(uri)
                ?.use { stream ->
                    stream.write(json.toByteArray())
                }

            Toast.makeText(context, "Backup successful", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(context, "Backup failed", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    suspend fun importBackup(
        context: Context,
        uri: Uri
    ) {
        val preImportTasks = dbOperation.taskGetAll()
        val preImportResetDate = dbOperation.getLastResetDate() ?: Utils.getCurrentDate()

        try {
            val json = context.contentResolver
                .openInputStream(uri)
                ?.bufferedReader()
                ?.readText()

            if (json != null) {
                val backupDto = Gson().fromJson(
                    json,
                    BackupDto::class.java
                )
                val taskDtos = backupDto.tasks
                val lastResetDate = backupDto.lastResetDate

                dbOperation.setLastResetToday(lastResetDate)

                val newTasks = taskDtos.map { dto ->
                    val task = dto.toTask()
                    return@map task.copy(parentId = null)
                }
                dbOperation.taskDeleteALl()
                dbOperation.saveNewTaskList(newTasks)
                val list = dbOperation.taskGetAll()
                val tasksWithParentId = taskDtos.map { dto ->
                    val task = dto.toTask()
                    return@map task
                }

                dbOperation.taskSaveList(tasksWithParentId)
                val list2 = dbOperation.taskGetAll()


                Toast.makeText(context, "Import successful", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            dbOperation.taskDeleteALl()
            dbOperation.taskSaveList(preImportTasks)
            dbOperation.setLastResetToday(preImportResetDate)
            Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    fun getAutoBackupPath(context: Context): String? {
        val uriString = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
            .getString("backup_uri", null) ?: return null

        val uri = uriString.toUri()
        // Extract just the folder name from the URI for display
        return DocumentFile.fromTreeUri(context, uri)?.name
    }

    fun setAutoBackupUri(context: Context, uri: Uri) {
        context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
            .edit {
                putString("backup_uri", uri.toString())
            }
    }


}

data class BackupDto(
    val lastResetDate: String,
    val tasks: List<TaskBackupDto>
)