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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tidy.ui.theme.TidyTheme
import io.objectbox.Box
import com.example.tidy.ui.component.TaskItem
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.navigation.compose.*
import com.example.tidy.ui.screen.AddTaskScreen
import com.example.tidy.ui.screen.BackupScreen
import com.example.tidy.ui.screen.HomeScreen
import com.example.tidy.ui.screen.SettingsScreen

class MainActivity : ComponentActivity() {
    private lateinit var taskBox: Box<Task>
    private lateinit var LastResetBox: Box<LastReset>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ObjectBox Box
        val app = application as App
        taskBox = app.boxStore.boxFor(Task::class.java)
        LastResetBox = app.boxStore.boxFor(LastReset::class.java)

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val currentRoute =
                navController.currentBackStackEntryAsState().value?.destination?.route

            TidyTheme {

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentRoute == "home",
                                onClick = { navController.navigate("home") },
                                icon = { Icon(Icons.Default.Home, null, modifier = Modifier.size(30.dp)) },
                                label = { Text("Home") }
                            )

                            NavigationBarItem(
                                selected = currentRoute == "settings",
                                onClick = { navController.navigate("settings") },
                                icon = { Icon(Icons.Default.Settings, null, modifier = Modifier.size(30.dp)) },
                                label = { Text("Settings") }
                            )
                        }
                    }
                ) { innerPadding ->


                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        composable("home") {
                            HomeScreen(taskBox, LastResetBox, navController)
                        }

                        composable("add_task") {
                            AddTaskScreen(taskBox, navController)
                        }

                        composable("settings") {
                            SettingsScreen( navController)
                        }

                        composable("backup_screen") {
                            BackupScreen(taskBox)
                        }
                    }
                }
            }
        }
    }
}