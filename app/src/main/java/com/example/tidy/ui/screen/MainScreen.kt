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

package com.example.tidy.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.tidy.constants.Routes
import com.example.tidy.ui.component.BottomBar
import com.example.tidy.viewModels.TaskViewModel

@Composable
fun MainScreen(viewModel: TaskViewModel) {
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
                HomeScreen(viewModel, navController)
            }

            composable(Routes.ADD_TASK) {
                AddTaskScreen(viewModel, navController)
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(navController)
            }

            composable(Routes.BACKUP) {
                BackupScreen(viewModel)
            }
        }
    }
}