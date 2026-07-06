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
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.WorkManager
import com.yourapp.db.AppDatabase

class App : Application() {

    lateinit var exportManager: ExportManager
        private set

    lateinit var database: AppDatabase
    override fun onCreate() {
        super.onCreate()
        database = createDatabase(this)


        exportManager = ExportManager(
            context = this,
            database = database
        )

        ProcessLifecycleOwner.get().lifecycle.addObserver(
            AppLifecycleObserver(exportManager)
        )

        val dbOperation = DbOperation(database, this)
        val config = Configuration.Builder()
            .setWorkerFactory(TidyWorkerFactory(dbOperation))
            .build()
        WorkManager.initialize(this, config)
    }
}

