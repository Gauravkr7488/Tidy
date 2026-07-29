package com.example.tidy

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.tidy.constants.RepeatTypes
import com.example.tidy.constants.TaskActions

class TidyWorker(
    context: Context,
    params: WorkerParameters,
    private val taskService: TaskService
) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return when (val action = inputData.getString("action")) {
            TaskActions.UNARCHIVE -> {
                val taskId = inputData.getLong("task_id", -1L)
                if (taskId == -1L) return Result.failure()
                val task = taskService.getTask(taskId) ?: return Result.failure()
                if (task.done == 1L || task.hide == 1L) {
                    taskService.saveTask(task.copy(done = 0, hide = 0, priority = 1))
                    Utils.sendNotification(
                        applicationContext,
                        title = "Schedule met",
                        message = "${task.title} Unarchived"
                    )
                }
                Result.success()
            }

            TaskActions.BACKUP -> {
                Utils.exportSilently(taskService, applicationContext)
                Utils.scheduleWork(
                    context = applicationContext,
                    scheduleTime = Utils.getAutoBackupTime(),
                    action = action,
                    taskId = null
                )
                Result.success()
            }

            TaskActions.RESET_ALARMS -> {
                val tasks = taskService.taskGetAll()
                tasks.forEach { task ->
                    if (task.repeatType == RepeatTypes.NONE && task.dueDateAndTime == null) return@forEach
                    taskService.saveTask(task)
                }
                Result.success()
            }

            else -> Result.failure()
        }
    }
}

class TidyWorkerFactory(private val taskService: TaskService) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return if (workerClassName == TidyWorker::class.java.name)
            TidyWorker(appContext, workerParameters, taskService)
        else null
    }
}