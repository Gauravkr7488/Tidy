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
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.tidy.constants.RepeatTypes
import com.google.gson.Gson
import com.tidy.sqldelight.BlockedTask
import com.tidy.sqldelight.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Collections.emptyList
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object Utils {
    fun getCurrentDate(): String {
        return SimpleDateFormat("dd", Locale.getDefault())
            .format(Calendar.getInstance().time) // gives "01", "02"
    }

    fun getCurrentDay(): String {
        return SimpleDateFormat("EEE", Locale.getDefault())
            .format(Calendar.getInstance().time)
            .uppercase() // gives "MON", "TUE" etc.
    }

    fun changeDateFormat(date: Long, pattern: String): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }.format(Date(date))
    }

    fun convertTimeToMillis(h: Int, m: Int): Long {
        return Calendar.getInstance(TimeZone.getDefault()).apply {
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun getCurrentDateMillis(): Long {
        return Calendar.getInstance(TimeZone.getDefault()).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }


    fun combineDateAndTimeMillis(date: Long?, time: Long?): Long? {
        if (date == null && time == null) return null
        val dateValue = date ?: getCurrentDateMillis()
        val timeValue = time ?: 0L

        val dateCalendar = Calendar.getInstance().apply {
            timeInMillis = dateValue
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val timeCalendar = Calendar.getInstance().apply {
            timeInMillis = timeValue
        }

        return dateCalendar.apply {
            set(
                Calendar.HOUR_OF_DAY,
                timeCalendar.get(Calendar.HOUR_OF_DAY)
            )
            set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
            set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND))
        }.timeInMillis
    }

    fun scheduleWork(context: Context, taskId: Long, scheduleTime: Long, action: String) {
        val delay = scheduleTime - System.currentTimeMillis()

        if (delay <= 0) return // Due date already passed

        val data = workDataOf("task_id" to taskId, "action" to action)

        val request = OneTimeWorkRequestBuilder<TidyWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("tidy-$taskId,$action") // Tag for cancellation
            .addTag("tidy-$taskId")
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    fun cancelDueDateWork(context: Context, taskId: Long, action: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag("tidy-$taskId,$action")
    }

    fun cancelAllWork(context: Context, taskId: Long) {
        WorkManager.getInstance(context).cancelAllWorkByTag("tidy-$taskId")
    }

    fun createBackupJson(
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

    fun getEmptyTask(): Task {
        return Task(
            id = 0,
            title = "",
            repeatType = RepeatTypes.NONE,
            repeatDays = "",
            description = "",
            done = 0,
            hide = 0,
            createdAt = System.currentTimeMillis(),
            parentId = null,
            blockStatus = 0,
            priority = null,
            dueDateAndTime = null,
            frequencyNumber = null,
            endDate = null,
            repeatAfterDone = 0,
        )
    }

    fun Task.toTaskDto(taskBlockString: String?): TaskBackupDto {
        return TaskBackupDto(
            id = id,
            title = title,
            done = done == 1L,
            repeatType = repeatType,
            repeatOn = repeatDays,
            description = description,
            hide = hide == 1L,
            createdAt = createdAt,
            parentId = parentId,
            blockedBy = taskBlockString,
            blockedStatus = blockStatus == 1L,
            priority = priority,
            dueDateAndTime = dueDateAndTime,
            frequencyNumber = frequencyNumber,
            endDate = endDate,
            repeatAfterDone = repeatAfterDone == 1L
        )
    }

    fun TaskBackupDto.toTask(): Task {
        return Task(
            id = id,
            title = title,
            done = if (done) 1L else 0L,
            repeatType = repeatType.uppercase(),
            repeatDays = repeatOn,
            description = description ?: "",
            hide = if (hide) 1L else 0L,
            parentId = parentId,
            blockStatus = if (blockedStatus) 1L else 0L,
            createdAt = createdAt,
            priority = priority,
            dueDateAndTime = dueDateAndTime,
            frequencyNumber = frequencyNumber,
            endDate = endDate,
            repeatAfterDone = if (repeatAfterDone) 1L else 0L,
        )
    }

    fun getBlockerFromString(blockString: String, id: Long): List<BlockedTask> {
        if (blockString.isEmpty()) return emptyList()
        val blockIds = blockString.split(",")
        if (blockIds.isEmpty()) return emptyList()
        val blockers: List<BlockedTask> = blockIds.filter { it.isNotEmpty() }.map {
            BlockedTask(
                task_id = id,
                blockedBy_id = it.trim().toLong()
            )
        }
        return blockers
    }

    suspend fun exportSilently(dbOperation: DbOperation, context: Context): Result<Unit> =
        withContext(Dispatchers.IO) {
            val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)

            return@withContext try {
                // TODO FIX
                val tasks = dbOperation.taskGetAll()
                var lastResetDate = dbOperation.getLastResetDate()
                if (lastResetDate == null) lastResetDate = getCurrentDate()
                val taskBlockers = dbOperation.getAllBlockers()
                val json = createBackupJson(tasks, lastResetDate, taskBlockers)

                val timestamp =
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
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