package com.example.tidy.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import io.objectbox.Box
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.tidy.LastReset
import com.example.tidy.Task
import com.example.tidy.constants.Routes
import com.example.tidy.ui.component.BottomBar

@Composable
fun MainScreen(taskBox: Box<Task>, lastResetBox: Box<LastReset>) {
    val navController = rememberNavController()
    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf(Routes.HOME, Routes.SETTINGS)) {
                BottomBar(navController, currentRoute)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(taskBox, lastResetBox, navController)
            }

            composable(Routes.ADD_TASK) {
                AddTaskScreen(taskBox, navController)
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(navController)
            }

            composable(Routes.BACKUP) {
                BackupScreen(taskBox)
            }
        }
    }
}