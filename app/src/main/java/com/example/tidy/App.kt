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

import android.app.Application
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.objectbox.BoxStore
import java.util.concurrent.TimeUnit

class App: Application() {
    lateinit var boxStore: BoxStore
        private set

    lateinit var exportManager: ExportManager
        private set

    override fun onCreate() {
        super.onCreate()
        // initialize ObjectBox
        boxStore = MyObjectBox.builder().androidContext(this).build()

        exportManager = ExportManager(
            context = this,
            taskBox = boxStore.boxFor(Task::class.java)
        )

        ProcessLifecycleOwner.get().lifecycle.addObserver(
            AppLifecycleObserver(exportManager)
        )

        schedulePeriodicBackup(this)
    }

    private fun schedulePeriodicBackup(context: Context) {
        val request = PeriodicWorkRequestBuilder<BackupWorker>(6, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "auto_backup",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}

