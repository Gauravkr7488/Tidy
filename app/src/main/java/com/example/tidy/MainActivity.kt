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

package com.example.tidy

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.tidy.constants.Options
import com.example.tidy.ui.component.dialog.TidyDialog
import com.example.tidy.ui.screen.MainScreen
import com.example.tidy.ui.theme.TidyTheme
import com.tidy.sqldelight.Task
import com.yourapp.db.AppDatabase
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase
    private var alarmTaskId: Long by mutableLongStateOf(-1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        handleIntent(intent)

        val app = application as App
        database = app.database
        enableEdgeToEdge()
        setContent {
            val alarmService = AlarmService(this)
            val scheduleService = ScheduleService(alarmService)
            val taskService = TaskService(db = database, scheduleService)
            TidyTheme {
                MainScreen(taskService)

                if (alarmTaskId != -1L) {
                    val scope = rememberCoroutineScope()
                    var task: Task = Utils.getEmptyTask()
                    LaunchedEffect(Unit) {
                        task = taskService.getTask(alarmTaskId)

                    }
                    TidyDialog(
                        title = "Schedule Met",
                        onDismissRequest = {},
                        buttons = {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        dismissAlarm()
                                        taskService.saveTask(task.copy(done = true, hide = false))
                                    }
                                }
                            ) {
                                Text("Mark Done")
                            }

                            TextButton(
                                onClick = {
                                    dismissAlarm()
                                }
                            ) {
                                Text("Dismiss")
                            }
                        }
                    ) {
                        Text("${task.title} schedule met")
                    }
                }
            }
        }
        createNotificationChannel(this)
        askNotificationPermission()
        Utils.requestExactAlarmPermission(this)
    }

    private fun dismissAlarm() {
        stopService(Intent(this, AlarmClockService::class.java))  // onDestroy stops sound + vibration
        intent.action = null   // so a rotation doesn't bring the dialog back
        alarmTaskId = -1
    }
    private fun handleIntent(intent: Intent?) {
        if (intent?.action == AlarmClockService.ACTION_OPEN_ALARM) {
            alarmTaskId = intent.getLongExtra(Options.TASK_ID, -1)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default_channel_id",
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for app notifications"
            }

            val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}