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
import com.tidy.sqldelight.BlockedTask
import com.tidy.sqldelight.Task

class BackupOperations(
    private val taskService: TaskService
) {
    suspend fun createBackup(
        context: Context,
        uri: Uri
    ) {
        try {
            val lastResetDate = taskService.getLastResetDate() ?: Utils.getCurrentDate()
            val taskBlockers = taskService.getAllBlockers()
            val json = createBackupJson(taskService.taskGetAll(), lastResetDate, taskBlockers)

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
        val preImportTasks = taskService.taskGetAll()
        val preImportResetDate = taskService.getLastResetDate() ?: Utils.getCurrentDate()

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

                taskService.setLastResetToday(lastResetDate)

                val newTasks: MutableList<Task> = mutableListOf()
                val blockList: MutableList<BlockedTask> = mutableListOf()
                taskDtos.forEach { taskBackupDto ->
                    val task = taskBackupDto.toTask()
                    val t = task.copy(parentId = null)
                    newTasks.add(t)
                    val blockString = taskBackupDto.blockedBy
                    if (blockString != null) {
                        val blockers = Utils.getBlockerFromString(blockString, taskBackupDto.id)
                        if (blockers.isNotEmpty()) {
                            blockList.addAll(blockers)
                        }
                    }
                }
                taskService.taskDeleteALl()
                newTasks.forEach { taskService.saveTaskWithId(it) }
                taskService.taskGetAll()
                val tasksWithParentId = taskDtos.map { dto ->
                    val task = dto.toTask()
                    return@map task
                }

                tasksWithParentId.forEach { taskService.saveTask(it) } //todo is this the right one?
                blockList.forEach {
                    val blockedTask = taskService.getTask(it.task_id) ?: return@forEach
                    taskService.saveTask(blockedTask.copy(blockStatus = 1L))
                    taskService.blockTask(it.task_id, it.blockedBy_id)
                }
                taskService.taskGetAll() // todo why is this here?


                Toast.makeText(context, "Import successful", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            taskService.taskDeleteALl()
            preImportTasks.forEach { taskService.saveTaskWithId(it) }
            taskService.setLastResetToday(preImportResetDate)
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