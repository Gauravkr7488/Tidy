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
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.example.tidy.Utils.toTask
import com.example.tidy.Utils.toTaskDto
import com.google.gson.Gson
import com.tidy.sqldelight.BlockedTask
import com.tidy.sqldelight.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale

class BackupService(
    private val taskService: TaskService,
    private val context: Context
) {
    suspend fun createBackup(uri: Uri) {
        try {
            val taskBlockers = taskService.getAllBlockers()
            val json = createBackupJson(taskService.taskGetAll(), taskBlockers)

            createFile(uri, json)

            Toast.makeText(context, "Backup successful", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(context, "Backup failed", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private suspend fun createFile(uri: Uri, json: String) = withContext(Dispatchers.IO) {
        context.contentResolver
            .openOutputStream(uri)
            ?.use { stream ->
                stream.write(json.toByteArray())
            }
    }

    suspend fun importBackup(
        uri: Uri
    ) {
        val preImportTasks = taskService.taskGetAll()

        try {
            val json = context.contentResolver
                .openInputStream(uri)
                ?.bufferedReader()
                ?.readText()

            if (json == null) return
            val backupDto = Gson().fromJson(
                json,
                BackupDto::class.java
            )
            val taskDtos = backupDto.tasks


            val newTasks: MutableList<Task> = mutableListOf()
            val blockList: MutableList<BlockedTask> = mutableListOf()
            taskDtos.forEach { taskBackupDto ->
                val task = taskBackupDto.toTask()
                newTasks.add(task)
                val blockString = taskBackupDto.blockedBy
                if (blockString != null) {
                    val blockers = getBlockerFromString(blockString, taskBackupDto.id)
                    if (blockers.isNotEmpty()) {
                        blockList.addAll(blockers)
                    }
                }
            }
            taskService.taskDeleteALl()
            newTasks.forEach { taskService.saveTaskWithId(it) }

            blockList.forEach {
                val blockedTask = taskService.getTask(it.task_id)
                taskService.saveTask(blockedTask.copy(blockStatus = 1L))
                taskService.blockTask(it.task_id, it.blockedBy_id)
            }

            Toast.makeText(context, "Import successful", Toast.LENGTH_SHORT).show()


        } catch (e: Exception) {
            taskService.taskDeleteALl()
            preImportTasks.forEach { taskService.saveTaskWithId(it) }
            Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun createBackupJson(
        tasks: List<Task>,
        taskBlocks: List<BlockedTask>
    ): String {
        val blockerList = taskBlocks.groupBy { it.task_id }
        val taskDtos = tasks.map { task ->
            val string =
                if (blockerList.containsKey(task.id)) blockerList[task.id]?.joinToString(",") { it.blockedBy_id.toString() } else null
            task.toTaskDto(string)
        }
        val backupDto = BackupDto( taskDtos)
        val json = Gson().toJson(backupDto)
        return json
    }

    private fun getBlockerFromString(blockString: String, id: Long): List<BlockedTask> {
        if (blockString.isEmpty()) return Collections.emptyList()
        val blockIds = blockString.split(",")
        if (blockIds.isEmpty()) return Collections.emptyList()
        val blockers: List<BlockedTask> = blockIds.filter { it.isNotEmpty() }.map {
            BlockedTask(
                task_id = id,
                blockedBy_id = it.trim().toLong()
            )
        }
        return blockers
    }
    suspend fun exportSilently() {
        val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
        try {
            val savedUri = prefs.getString("backup_uri", null)?.toUri()
                ?: throw Exception("Failed to get saved Uri")
            val docTree = DocumentFile.fromTreeUri(context, savedUri)
            val timestamp =
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "tidy_backup_$timestamp.json"
            val file = docTree?.createFile("application/json", fileName)
            val fileUri = file?.uri ?: throw Exception("failed to get file uri")
            createBackup(fileUri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}