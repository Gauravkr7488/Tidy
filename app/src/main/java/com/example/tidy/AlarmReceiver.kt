package com.example.tidy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.tidy.constants.Options

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(Options.TASK_ID, -1)
        val action = intent.getStringExtra(Options.ACTION) ?: return
        Utils.scheduleImmediateWork(context, taskId, action)
    }
}