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
package com.example.tidy.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tidy.constants.Routes
import kotlinx.coroutines.launch

@Composable
fun BottomBar( currentRoute: String, pagerState: PagerState) {
    val scope = rememberCoroutineScope()

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.HOME,
            onClick = {
                if (currentRoute == Routes.HOME) return@NavigationBarItem
                scope.launch {
                    pagerState.scrollToPage(0)
                }
            },
            icon = {
                Icon(
                    Icons.Default.Home,
                    "Home Button",
                    modifier = Modifier.size(30.dp)
                )
            },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.MENU,
            onClick = {
                if (currentRoute == Routes.MENU) return@NavigationBarItem
                scope.launch {
                    pagerState.scrollToPage(1)
                }
            },
            icon = {
                Icon(
                    Icons.Default.Apps,
                    null,
                    modifier = Modifier.size(30.dp)
                )
            },
            label = { Text("Menu") }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.SETTINGS,
            onClick = {
                if (currentRoute == Routes.SETTINGS) return@NavigationBarItem
                scope.launch {
                    pagerState.scrollToPage(2)
                }
            },
            icon = {
                Icon(
                    Icons.Default.Settings,
                    null,
                    modifier = Modifier.size(30.dp)
                )
            },
            label = { Text("Settings") }
        )
    }
}