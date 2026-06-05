package com.example.tidy

import com.example.tidy.constants.RepeatTypes
import com.google.gson.Gson
import com.tidy.sqldelight.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
            serialNo = null,
            priority = null,
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
            serialNo = serialNo,
            priority = priority,
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
            serialNo = serialNo,
            priority = priority,
        )
    }
}