package com.example.tidy

import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.tidy.sqldelight.Task

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

    fun createBackupJson(tasks: List<Task>, lastResetDate: String): String {
        val taskDtos = tasks.map { it.toTaskDto() }
        val backupDto = BackupDto(lastResetDate, taskDtos)
        val json = Gson().toJson(backupDto)
        return json
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
            parentId = parentId
        )
    }

    fun TaskBackupDto.toTask(): Task {
        return Task(
            id = 0,
            title = title,
            done = if (done) 1L else 0L,
            repeatType = repeatType,
            repeatDays = repeatOn,
            description = description ?: "",
            hide = if (hide) 1L else 0L,
            parentId = parentId,
            createdAt = createdAt
        )
    }
}