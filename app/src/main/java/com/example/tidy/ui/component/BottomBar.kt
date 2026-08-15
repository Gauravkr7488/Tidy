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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Bottom navigation bar driven entirely by [pagerState].
 *
 * Tab → page index mapping:
 *   0 = Home
 *   1 = Search
 *   2 = Notes
 *   3 = Settings
 */
@Composable
fun BottomBar(pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage

    NavigationBar {
        NavigationBarItem(
            selected = currentPage == 0,
            onClick = {
                if (currentPage != 0) scope.launch { pagerState.scrollToPage(0) }
            },
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(28.dp)
                )
            },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentPage == 1,
            onClick = {
                if (currentPage != 1) scope.launch { pagerState.scrollToPage(1) }
            },
            icon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(28.dp)
                )
            },
            label = { Text("Search") }
        )
        NavigationBarItem(
            selected = currentPage == 2,
            onClick = {
                if (currentPage != 2) scope.launch { pagerState.scrollToPage(2) }
            },
            icon = {
                Icon(
                    Icons.Default.NoteAlt,
                    contentDescription = "Notes",
                    modifier = Modifier.size(28.dp)
                )
            },
            label = { Text("Notes") }
        )
        NavigationBarItem(
            selected = currentPage == 3,
            onClick = {
                if (currentPage != 3) scope.launch { pagerState.scrollToPage(3) }
            },
            icon = {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(28.dp)
                )
            },
            label = { Text("Settings") }
        )
    }
}