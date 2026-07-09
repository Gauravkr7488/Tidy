package com.example.tidy

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.tidy.constants.RepeatTypes
import com.google.gson.Gson
import com.tidy.sqldelight.BlockedTask
import com.tidy.sqldelight.Task
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
        )
    }

    fun Task.toTaskDto(taskBlockString: String?): TaskBackupDto {
        return TaskBackupDto(
            id = id,
            title = title,
            done = done != 0L,
            repeatType = repeatType,
            repeatOn = repeatDays,
            description = description,
            hide = hide != 0L,
            createdAt = createdAt,
            parentId = parentId,
            blockedBy = taskBlockString,
            blockedStatus = blockStatus != 0L,
            priority = priority,
            dueDateAndTime = dueDateAndTime,
            frequencyNumber = frequencyNumber,
            endDate = endDate,
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
}