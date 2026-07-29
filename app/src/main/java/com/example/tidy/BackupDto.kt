package com.example.tidy

data class BackupDto(
    val lastResetDate: String,
    val tasks: List<TaskBackupDto>
)
