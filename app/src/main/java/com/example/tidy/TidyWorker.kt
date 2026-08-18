/*
 * Copyright (C) 2026  Gaurav Kumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.example.tidy

import android.content.Context
import androidx.core.content.edit
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
                val task = taskService.getTask(taskId)
                if (task.done) {
                    taskService.saveTask(task.copy(done = false, hide = false))
                    if (task.repeatType == RepeatTypes.DAY){
                        setDailyUnarchivedCount()
                    }else{
                        Utils.sendNotification(
                            applicationContext,
                            title = "Schedule met",
                            message = "${task.title} Unarchived"
                        )
                    }
                }
                Result.success()
            }

            TaskActions.BACKUP -> {
                val backupService = BackupService(taskService, applicationContext)
                backupService.exportSilently()
                val workService = WorkService(applicationContext)
                workService.scheduleWork(
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

            TaskActions.RESET_DAY -> {
                val count = taskService.resetSkippedTasks()
                if (count > 0) {
                    Utils.sendNotification(
                        context = applicationContext,
                        title = "Skipped tasks Unarchived",
                        message = "$count tasks unarchived"
                    )
                }
                taskService.archiveAndRescheduleNonDoneDailyTasksWithDueTime()
                val alarmService = AlarmService(applicationContext)
                alarmService.scheduleAlarm(
                    scheduleTime = Utils.getNextMidNightMilli() + 10000, // delay for daily unarchival count update
                    action = TaskActions.RESET_DAY,
                    taskId = -1
                )
                val dailyCount = getAndResetDailyUnarchivedCount()
                if (dailyCount > 0L) {
                    Utils.sendNotification(
                        context = applicationContext,
                        title = "Daily tasks Unarchived",
                        message = "$dailyCount tasks unarchived"
                    )
                }
                Result.success()
            }

            else -> Result.failure()
        }
    }
    @Synchronized
    private fun setDailyUnarchivedCount() {
        val prefs = applicationContext.getSharedPreferences("count_prefs", Context.MODE_PRIVATE)
        val x = prefs.getLong("count_daily_unarchive", 0)
        val y = x + 1
        prefs.edit {
            putLong("count_daily_unarchive", y)
        }
    }

    private fun getAndResetDailyUnarchivedCount(): Long {
        val prefs = applicationContext.getSharedPreferences("count_prefs", Context.MODE_PRIVATE)
        val count = prefs.getLong("count_daily_unarchive", 0)
        prefs.edit {
            putLong("count_daily_unarchive", 0)
        }
        return count
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