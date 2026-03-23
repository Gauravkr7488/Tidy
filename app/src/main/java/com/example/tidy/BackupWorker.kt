package com.example.tidy

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class BackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val exportManager = (applicationContext as App).exportManager
            exportManager.exportSilently()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}