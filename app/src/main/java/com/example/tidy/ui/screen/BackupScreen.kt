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

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tidy.Task
import com.example.tidy.ui.component.SimpleCard
import com.google.gson.Gson
import io.objectbox.Box

@Composable
fun BackupScreen(
    taskBox: Box<Task>,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val listState = rememberLazyListState()
    val exportLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            uri?.let { createBackup(context, taskBox, it) }
        }

    val importLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            uri?.let { importBackup(context, taskBox, it) }
        }
    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(start = 16.dp, end = 16.dp)
        ) {

            Text(
                text = "Backup",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    SimpleCard(
                        {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Export",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                IconButton(
                                    modifier = Modifier
                                        .padding(8.dp)                     // outer spacing
                                        .size(48.dp),
                                    onClick = {
                                        exportLauncher.launch("backup.json")
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Upload,
                                        contentDescription = "Export"
                                    )
                                }
                            }
                        }
                    )
                    SimpleCard(
                        {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Import",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                IconButton(
                                    modifier = Modifier
                                        .padding(8.dp)                     // outer spacing
                                        .size(48.dp),
                                    onClick = {
                                        importLauncher.launch(arrayOf("application/json"))
                                    }

                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Import"
                                    )
                                }
                            }
                        }
                    )

                }
            }
        }
    }
}

fun createBackup(
    context: Context,
    taskBox: Box<Task>,
    uri: Uri
) {
    try {
        val tasks = taskBox.all
        val json = Gson().toJson(tasks)

        context.contentResolver
            .openOutputStream(uri)
            ?.use { stream ->
                stream.write(json.toByteArray())
            }

        Toast.makeText(context, "Backup successful", Toast.LENGTH_SHORT).show()

    } catch (e: Exception) {
        Toast.makeText(context, "Backup failed", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}

fun importBackup(
    context: Context,
    taskBox: Box<Task>,
    uri: Uri
) {
    try {
        val json = context.contentResolver
            .openInputStream(uri)
            ?.bufferedReader()
            ?.readText()

        if (json != null) {
            val tasks = Gson().fromJson(
                json,
                Array<Task>::class.java
            ).toList()

            taskBox.removeAll()
            val newTasks = tasks.map {
                it.copy(id = 0)
            }
            taskBox.put(newTasks)

            Toast.makeText(context, "Import successful", Toast.LENGTH_SHORT).show()
        }

    } catch (e: Exception) {
        Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}