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
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class WorkService(private val context: Context) {
    fun scheduleWork(taskId: Long?, scheduleTime: Long, action: String) {
        val delay = scheduleTime - System.currentTimeMillis()

        if (delay <= 0) return // Due date already passed

        val data = workDataOf("task_id" to taskId, "action" to action)

        val request = OneTimeWorkRequestBuilder<TidyWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("tidy-$action") // Tag for cancellation
            .addTag("tidy-$taskId")
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    fun scheduleImmediateWork(taskId: Long?, action: String) {
        val data = workDataOf("task_id" to taskId, "action" to action)

        val request = OneTimeWorkRequestBuilder<TidyWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(data)
            .addTag("tidy-$action") // Tag for cancellation
            .addTag("tidy-$taskId")
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    fun cancelAllWorkByAction(action: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag("tidy-$action")
    }

    fun schedulePeriodicWork(action: String, intervalInMilli: Long, label: String, initialDelayInMilli: Long) {
        val data = workDataOf("action" to action)
        val request = PeriodicWorkRequestBuilder<TidyWorker>(intervalInMilli, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("tidy-$action")
            .setInitialDelay(initialDelayInMilli, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            label,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

//    fun cancelPeriodicWork(label: String) {
//        WorkManager.getInstance(context).cancelUniqueWork(label)
//    }
}