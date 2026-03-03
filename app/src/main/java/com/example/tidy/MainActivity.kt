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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tidy.ui.theme.TidyTheme
import io.objectbox.Box
import com.example.tidy.ui.screen.MainScreen

class MainActivity : ComponentActivity() {
    private lateinit var taskBox: Box<Task>
    private lateinit var lastResetBox: Box<LastReset>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ObjectBox Box
        val app = application as App
        taskBox = app.boxStore.boxFor(Task::class.java)
        lastResetBox = app.boxStore.boxFor(LastReset::class.java)

        enableEdgeToEdge()
        setContent {
            TidyTheme {
                MainScreen(taskBox, lastResetBox)
            }
        }
    }
}