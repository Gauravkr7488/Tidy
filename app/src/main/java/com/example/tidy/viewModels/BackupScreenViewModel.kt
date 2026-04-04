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

package com.example.tidy.viewModels

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tidy.DbOperation
import com.example.tidy.Task
import com.example.tidy.TaskDto
import com.example.tidy.toDto
import com.example.tidy.toTask
import com.google.gson.Gson
import kotlinx.coroutines.launch

class BackupScreenViewModel(
    private val dbOperation: DbOperation
) : ViewModel() {
    fun createBackup(
        context: Context,
        uri: Uri
    ) {
        viewModelScope.launch {
            try {
                val tasks = dbOperation.taskGetAll()
                val dtoTasks = tasks.map { it.toDto() }
                val json = Gson().toJson(dtoTasks)

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
    }

    fun importBackup(
        context: Context,
        uri: Uri
    ) {
        viewModelScope.launch {
            val oldTasks = dbOperation.taskGetAll()

            try {
                val json = context.contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader()
                    ?.readText()

                if (json != null) {
                    val dtos = Gson().fromJson(
                        json,
                        Array<TaskDto>::class.java
                    ).toList()


                    val idMap = mutableMapOf<Long, Task>()

                    val newTasks = dtos.map { dto ->
                        val task = dto.toTask()
                        idMap[dto.id] = task
                        task
                    }

                    dbOperation.taskDeleteALl()
                    dbOperation.taskSaveList(newTasks)// to get new ids

//              relation work
                    dtos.forEach { dto ->
                        val task = idMap[dto.id] ?: return@forEach

                        val children = dto.childTasks?.mapNotNull { oldChildId ->
                            idMap[oldChildId]
                        } ?: emptyList()

                        task.children.addAll(children)
                    }
                    dbOperation.taskSaveList(newTasks)

                    Toast.makeText(context, "Import successful", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                dbOperation.taskDeleteALl()
                dbOperation.taskSaveList(oldTasks)
                Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    fun getAutoBackupPath(context: Context): String? {
        val uriString = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
            .getString("backup_uri", null) ?: return null

        val uri = uriString.toUri()
        // Extract just the folder name from the URI for display
        return DocumentFile.fromTreeUri(context, uri)?.name
    }

    fun setAutoBackupUri(context: Context, uri: Uri) {
        context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
            .edit {
                putString("backup_uri", uri.toString())
            }
    }


}