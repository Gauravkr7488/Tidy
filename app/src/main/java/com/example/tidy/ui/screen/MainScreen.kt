package com.example.tidy.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import io.objectbox.Box
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.tidy.LastReset
import com.example.tidy.Task
import com.example.tidy.ui.component.BottomBar

@Composable
fun MainScreen(taskBox: Box<Task>, lastResetBox: Box<LastReset>) {
    val navController = rememberNavController()
    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf("home", "settings")) {
                BottomBar(navController, currentRoute)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(taskBox, lastResetBox, navController)
            }

            composable("add_task") {
                AddTaskScreen(taskBox, navController)
            }

            composable("settings") {
                SettingsScreen(navController)
            }

            composable("backup_screen") {
                BackupScreen(taskBox)
            }
        }
    }
}