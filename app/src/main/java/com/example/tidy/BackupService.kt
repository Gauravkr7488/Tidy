package com.example.tidy

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.example.tidy.Utils.getCurrentDate
import com.example.tidy.Utils.toTask
import com.example.tidy.Utils.toTaskDto
import com.google.gson.Gson
import com.tidy.sqldelight.BlockedTask
import com.tidy.sqldelight.Task
import java.util.Collections

class BackupService(
    private val taskService: TaskService,
    private val context: Context
) {
    suspend fun createBackup(uri: Uri) {
        try {
            val lastResetDate = taskService.getLastResetDate() ?: getCurrentDate()
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
        uri: Uri
    ) {
        val preImportTasks = taskService.taskGetAll()
        val preImportResetDate = taskService.getLastResetDate() ?: getCurrentDate()

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
            val lastResetDate = backupDto.lastResetDate

            taskService.setLastResetToday(lastResetDate)

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
            taskService.setLastResetToday(preImportResetDate)
            Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun createBackupJson(
        tasks: List<Task>,
        lastResetDate: String,
        taskBlocks: List<BlockedTask>
    ): String {
        val blockerList = taskBlocks.groupBy { it.task_id }
        val taskDtos = tasks.map { task ->
            val string =
                if (blockerList.containsKey(task.id)) blockerList[task.id]?.joinToString(",") { it.blockedBy_id.toString() } else null
            task.toTaskDto(string)
        }
        val backupDto = BackupDto(lastResetDate, taskDtos)
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

    fun getAutoBackupPath(): String? {
        val uriString = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
            .getString("backup_uri", null) ?: return null

        val uri = uriString.toUri()
        // Extract just the folder name from the URI for display
        return DocumentFile.fromTreeUri(context, uri)?.name
    }


    fun setAutoBackupUri(uri: Uri) {
        context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
            .edit {
                putString("backup_uri", uri.toString())
            }
    }
}