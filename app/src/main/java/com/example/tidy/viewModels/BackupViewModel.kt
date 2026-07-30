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
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tidy.BackupService
import com.example.tidy.Utils
import com.example.tidy.WorkService
import com.example.tidy.constants.TaskActions
import kotlinx.coroutines.launch

class BackupViewModel(
    private val backupService: BackupService,
    private val workService: WorkService
) : ViewModel() {

    fun createBackup(uri: Uri) {
        viewModelScope.launch {
            backupService.createBackup(uri)
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            backupService.importBackup(uri)
        }
    }

    fun getBackupFolderName(context: Context): String? {
        val uriString = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
            .getString("backup_uri", null) ?: return null

        val uri = uriString.toUri()
        // Extract just the folder name from the URI for display
        return DocumentFile.fromTreeUri(context, uri)?.name
    }


    fun setAutoBackupUri(uri: Uri, context: Context) {
        context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
            .edit {
                putString("backup_uri", uri.toString())
            }
    }

    fun setAutoBackup() {
        workService.cancelAllWorkByAction(TaskActions.BACKUP)
        workService.scheduleWork(
            scheduleTime = Utils.getAutoBackupTime(),
            action = TaskActions.BACKUP,
            taskId = null
        )
    }
}