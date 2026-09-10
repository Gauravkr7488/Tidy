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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tidy.constants.RepeatTypes
import com.example.tidy.ui.component.textField.SearchTextField
import com.tidy.sqldelight.Task

@Composable
fun SearchTasksTidy(
    tasks: List<Task>,
    onQueryChange: (String) -> Unit,
    onFilteredTasksChanged: (List<Task>) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var includeFilters: List<String> by remember { mutableStateOf(emptyList()) }
    var excludeFilters: List<String> by remember { mutableStateOf(emptyList()) }
    val filterList = listOf("Repeat", "Parents", "Archived", "Skipped", "Done")
    val filteredTasks = tasks.filter { task ->
        val matchesQuery = query.isBlank() ||
                task.title.contains(query, ignoreCase = true) ||
                task.description.contains(query, ignoreCase = true)
        val matchesFilter = includeFilters.isEmpty() ||
                includeFilters.all { filter ->
                    when (filter) {
                        "Repeat" -> task.repeatType != RepeatTypes.NONE
                        "Parents" -> task.parentId == null
                        "Archived" -> task.hide
                        "Skipped" -> task.skipStatus
                        "Done" -> task.done
                        else -> false
                    }
                }
        val excludeFilter = excludeFilters.any {
            when (it) {
                "Repeat" -> task.repeatType != RepeatTypes.NONE
                "Parents" -> task.parentId == null
                "Archived" -> task.hide
                "Skipped" -> task.skipStatus
                "Done" -> task.done
                else -> false
            }
        }
        matchesQuery && matchesFilter && !excludeFilter
    }

    LaunchedEffect(filteredTasks) {
        onFilteredTasksChanged(filteredTasks)
    }

    SearchTextField(
        query = query,
        placeHolder = "Search tasks",
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        @Suppress("AssignedValueIsNeverRead")
        query = it
        onQueryChange(it)
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filterList.forEach { filter ->
            item {
                FilterChip(
                    selected = includeFilters.contains(filter),
                    onClick = {
                        if (includeFilters.contains(filter)) {
                            includeFilters -= filter
                            excludeFilters += filter
                        } else if (excludeFilters.contains(filter)) {
                            excludeFilters -= filter
                        } else {
                            includeFilters += filter
                        }
                    },
                    label = { Text(filter) },
                    colors = if (excludeFilters.contains(filter)) FilterChipDefaults.filterChipColors(
                        MaterialTheme.colorScheme.surfaceVariant
                    ) else FilterChipDefaults.filterChipColors()
                )
            }
        }
    }
}