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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tidy.Task
import com.google.gson.Gson
import io.objectbox.Box

@Composable
fun BackupScreen(
    taskBox: Box<Task>,
) {

    val context = LocalContext.current

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Text(
            text = "Backup",
            style = MaterialTheme.typography.headlineMedium
        )

        // Export Section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Export",
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = {
                    exportLauncher.launch("backup.json")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export Backup")
            }
        }

        // Import Section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Import",
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = {
                    importLauncher.launch(arrayOf("application/json"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import Backup")
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