package com.example.tidy.ui.screen

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tidy.Task
import com.google.gson.Gson
import io.objectbox.Box
import io.objectbox.BoxStore
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    taskBox: Box<Task>
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Button(
                onClick = {
                    exportData(taskBox, context)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export Data")
            }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.Q)
fun exportData(taskBox: Box<Task>, context: Context) {

    val allTasks = taskBox.all
    val gson = Gson()
    val json = gson.toJson(allTasks)

    val fileName = "backup_tasks.json"

    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
    }

    val uri = resolver.insert(
        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
        contentValues
    )

    uri?.let {
        resolver.openOutputStream(it)?.use { outputStream ->
            outputStream.write(json.toByteArray())
        }

        Toast.makeText(
            context,
            "Saved to Downloads",
            Toast.LENGTH_LONG
        ).show()
    }
}