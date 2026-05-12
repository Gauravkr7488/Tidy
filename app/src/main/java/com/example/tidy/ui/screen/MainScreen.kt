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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tidy.DbOperation
import com.example.tidy.ExportManager
import com.example.tidy.constants.Routes
import com.example.tidy.ui.component.BottomBar
import com.example.tidy.viewModels.AddTaskScreenViewModel
import com.example.tidy.viewModels.BackupScreenViewModel
import com.example.tidy.viewModels.HomeScreenViewModel
import kotlinx.coroutines.launch

@Composable
fun MainScreen(dbOperation: DbOperation, exportManager: ExportManager) {
    val navController = rememberNavController()
    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    val addTaskScreenViewModel = viewModel<AddTaskScreenViewModel>(
        factory = viewModelFactory {
            initializer { AddTaskScreenViewModel(dbOperation) }
        }
    )
    val homeScreenViewModel = viewModel<HomeScreenViewModel>(
        factory = viewModelFactory {
            initializer {
                HomeScreenViewModel(
                    dbOperation,
                    exportManager
                )
            }
        }
    )
    val backupScreenViewModel = BackupScreenViewModel(dbOperation)

    val tabs = listOf(Routes.HOME, Routes.SEARCH, Routes.SETTINGS)
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val currentPage = tabs[pagerState.currentPage]
    val scope = rememberCoroutineScope()

    BackHandler(
        enabled = !pagerState.isScrollInProgress && pagerState.currentPage in 1..2 // only Menu & Settings
    ) {
        scope.launch {
            pagerState.scrollToPage(0)
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute == Routes.HOME) {
                BottomBar(currentPage, pagerState)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(Routes.HOME) {
                Box {
                    HorizontalPager(
                        state = pagerState,
                        beyondViewportPageCount = 2, // keeps all 3 pages alive
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> HomeScreen(homeScreenViewModel, navController)
                            1 -> SearchScreen(homeScreenViewModel, navController)
                            2 -> SettingsScreen(navController)
                        }
                    }
                }
            }

            composable("${Routes.ADD_TASK}/{taskId}") { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId")?.toLong()
                if (taskId == null) {
                    AddTaskScreen(
                        addTaskScreenViewModel,
                        navController,
                    )
                } else {
                    AddTaskScreen(
                        addTaskScreenViewModel,
                        navController,
                        taskId = taskId,
                    )
                }
            }

            composable(Routes.ADD_TASK) {
                AddTaskScreen(
                    addTaskScreenViewModel,
                    navController,
                )
            }

            composable(Routes.BACKUP) {
                BackupScreen(backupScreenViewModel)
            }

            composable(Routes.SEARCH) {
                SearchScreen(homeScreenViewModel, navController)
            }
        }
    }
}