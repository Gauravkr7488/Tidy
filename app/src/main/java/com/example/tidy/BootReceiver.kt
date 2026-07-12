package com.example.tidy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.tidy.constants.TaskActions

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Utils.scheduleImmediateWork(
                context = context,
                taskId = null,
                action = TaskActions.RESET_ALARMS
            )
        }
    }
}