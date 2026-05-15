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
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ProcessLifecycleOwner
import com.yourapp.db.AppDatabase
import io.objectbox.BoxStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class App: Application() {
    lateinit var boxStore: BoxStore
        private set

    lateinit var exportManager: ExportManager
        private set
    lateinit var database: AppDatabase
    override fun onCreate() {
        super.onCreate()
        database = createDatabase(this)

        boxStore = MyObjectBox.builder().androidContext(this).build()

        exportManager = ExportManager(
            context = this,
           database = database
        )

        CoroutineScope(Dispatchers.IO).launch {
            runMigrationIfNeeded(
                context = this@App,
                objectBox = boxStore,
                database = database
            )
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(
            AppLifecycleObserver(exportManager)
        )
    }

    suspend fun runMigrationIfNeeded(
        context: Context,
        objectBox: BoxStore,
        database: AppDatabase
    ) {

        val migrated = context.dataStore.data
            .map { prefs ->
                prefs[MIGRATED_TO_SQL] ?: false
            }
            .first()

        if (migrated) return

        migrate(objectBox, database)

        context.dataStore.edit { prefs ->
            prefs[MIGRATED_TO_SQL] = true
        }
    }

    fun migrate(
        objectBox: BoxStore,
        database: AppDatabase
    ) {

        val taskBox = objectBox.boxFor(Task::class.java)

        taskBox.all.forEach { task ->

            database.taskQueries.insertTask(
                id = task.id,
                title = task.title,
                done = if (task.done) 1 else 0,
                repeatType = task.repeatType,
                repeatDays = task.repeatDays,
                description = task.description,
                hide = if (task.hide) 1 else 0,
                createdAt = task.createdAt,
                parentId = task.parent.targetId
                    .takeIf { it != 0L }
            )
        }
    }
}

