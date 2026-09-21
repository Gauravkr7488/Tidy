package com.example.tidy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.tidy.constants.Options
import com.example.tidy.constants.TaskActions

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(Options.TASK_ID, -1)
        val action = intent.action ?: return
        if (action == TaskActions.ALARM_CLOCK) {
            val taskName = intent.getStringExtra(Options.TASK_NAME)
            val serviceIntent = Intent(context, AlarmClockService::class.java)
                .putExtra(Options.TASK_NAME, taskName)
                .putExtra(Options.TASK_ID, taskId)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
        val workService = WorkService(context)
        workService.scheduleImmediateWork(taskId, action)
    }
}