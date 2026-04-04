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
import io.objectbox.BoxStore

class App: Application() {
    lateinit var boxStore: BoxStore
        private set

    lateinit var exportManager: ExportManager
        private set

    override fun onCreate() {
        super.onCreate()
        boxStore = MyObjectBox.builder().androidContext(this).build()

        exportManager = ExportManager(
            context = this,
            taskBox = boxStore.boxFor(Task::class.java)
        )

        ProcessLifecycleOwner.get().lifecycle.addObserver(
            AppLifecycleObserver(exportManager)
        )
    }
}

