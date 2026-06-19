package com.example.tidy

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.tidy.constants.RepeatTypes
import com.google.gson.Gson
import com.tidy.sqldelight.Task
import java.text.SimpleDateFormat
import java.util.Calendar
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

    fun scheduleDueDateWork(context: Context, taskId: Long, dueDateMillis: Long) {
        val delay = dueDateMillis - System.currentTimeMillis()

        if (delay <= 0) return // Due date already passed

        val data = workDataOf("task_id" to taskId)

        val request = OneTimeWorkRequestBuilder<DueDateWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("due_date_$taskId") // Tag for cancellation
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    fun cancelDueDateWork(context: Context, taskId: Long) {
        WorkManager.getInstance(context).cancelAllWorkByTag("due_date_$taskId")
    }

    fun createBackupJson(tasks: List<Task>, lastResetDate: String): String {
        val taskDtos = tasks.map { it.toTaskDto() }
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
            priority = null,
            dueDateAndTime = null
        )
    }

    fun Task.toTaskDto(): TaskBackupDto {
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
            priority = priority,
            dueDateAndTime = dueDateAndTime
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
            createdAt = createdAt,
            priority = priority,
            dueDateAndTime = dueDateAndTime
        )
    }
}