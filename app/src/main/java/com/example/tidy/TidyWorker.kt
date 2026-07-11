package com.example.tidy

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.tidy.constants.TaskActions

class TidyWorker(context: Context, params: WorkerParameters, private val dbOperation: DbOperation) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val action = inputData.getString("action")

        return when (action) {
            TaskActions.UNARCHIVE -> {
                val taskId = inputData.getLong("task_id", -1L)
                if (taskId == -1L) return Result.failure()
                val task = dbOperation.getTask(taskId) ?: return Result.failure()
                dbOperation.saveTask(task.copy(done = 0, hide = 0, priority = 1))
                Result.success()
            }

            TaskActions.BACKUP -> {
                Utils.exportSilently(dbOperation, applicationContext)
                Utils.scheduleWork(
                    context = applicationContext,
                    scheduleTime = Utils.getAutoBackupTime(),
                    action = TaskActions.BACKUP,
                    taskId = null
                )
                Result.success()
            }

            else -> Result.failure()
        }
    }
}

class TidyWorkerFactory(private val dbOperation: DbOperation) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return if (workerClassName == TidyWorker::class.java.name)
            TidyWorker(appContext, workerParameters, dbOperation)
        else null
    }
}