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
                                icon = { Icon(Icons.Default.Home, null) },
                                label = { Text("Home") }
                            )

                            NavigationBarItem(
                                selected = currentRoute == "settings",
                                onClick = { navController.navigate("settings") },
                                icon = { Icon(Icons.Default.Settings, null) },
                                label = { Text("settings") }
                            )
                        }
                    }
                ) { innerPadding ->


                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding) // ✅ correct place
                    ) {

                        composable("home") {
                            HomeScreen(navController, taskBox, LastResetBox)
                        }

                        composable("add_task") {
                            AddTaskScreen(taskBox, navController)
                        }

                        composable("settings") {
                            SettingsScreen(taskBox)
                        }
                    }

                }
            }
        }
    }
}