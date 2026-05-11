package com.example.tidy

import com.example.tidy.viewModels.BackupDto
import com.google.gson.Gson
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
}