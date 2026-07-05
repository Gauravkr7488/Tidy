package com.example.tidy

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters

class DueDateWorker(context: Context, params: WorkerParameters, private val dbOperation: DbOperation) : CoroutineWorker(context, params){
    override suspend fun doWork(): Result {
        val taskId = inputData.getLong("task_id", -1L)

        if (taskId == -1L) return Result.failure()

        val task = dbOperation.getTask(taskId) ?: return Result.failure()
        dbOperation.saveTask(task.copy(priority = 1))

        return Result.success()
    }
}

class DueDateWorkerFactory(private val dbOperation: DbOperation) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return if (workerClassName == DueDateWorker::class.java.name)
            DueDateWorker(appContext, workerParameters, dbOperation)
        else null
    }
}