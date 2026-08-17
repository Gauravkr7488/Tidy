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
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.Ace777.tidy.R
import com.example.tidy.constants.RepeatTypes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tidy.sqldelight.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Utils {
    fun changeDateFormat(date: Long, pattern: String): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }.format(Date(date))
    }

    fun convertTimeToMillis(h: Int, m: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun getCurrentDateMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }


    fun combineDateAndTimeMillis(date: Long?, time: Long?): Long? {
        if (date == null && time == null) return null
        val dateValue = date ?: getCurrentDateMillis()
        val timeValue = time ?: getCurrentDateMillis()

        val dateCalendar = Calendar.getInstance().apply {
            timeInMillis = dateValue
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val timeCalendar = Calendar.getInstance().apply {
            timeInMillis = timeValue
        }

        return dateCalendar.apply {
            set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
            set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND))
        }.timeInMillis
    }

    fun scheduleImmediateWork(context: Context, taskId: Long?, action: String) {
        val data = workDataOf("task_id" to taskId, "action" to action)

        val request = OneTimeWorkRequestBuilder<TidyWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(data)
            .addTag("tidy-$action") // Tag for cancellation
            .addTag("tidy-$taskId")
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    fun getEmptyTask(): Task {
        return Task(
            id = 0,
            title = "",
            repeatType = RepeatTypes.NONE,
            repeatDays = "",
            description = "",
            done = false,
            hide = false,
            createdAt = System.currentTimeMillis(),
            parentId = null,
            blockStatus = false,
            priority = null,
            dueDateAndTime = null,
            frequencyNumber = null,
            endDate = null,
            repeatAfterDone = false,
        )
    }

    fun Task.toTaskDto(taskBlockString: String?): TaskBackupDto {
        return TaskBackupDto(
            id = id,
            title = title,
            done = done,
            repeatType = repeatType,
            repeatOn = repeatDays,
            description = description,
            hide = hide,
            createdAt = createdAt,
            parentId = parentId,
            blockedBy = taskBlockString,
            blockedStatus = blockStatus,
            priority = priority,
            dueDateAndTime = dueDateAndTime,
            frequencyNumber = frequencyNumber,
            endDate = endDate,
            repeatAfterDone = repeatAfterDone
        )
    }

    fun TaskBackupDto.toTask(): Task {
        return Task(
            id = id,
            title = title,
            done = done,
            repeatType = repeatType.uppercase(),
            repeatDays = repeatOn,
            description = description ?: "",
            hide = hide,
            parentId = parentId,
            blockStatus = blockedStatus,
            createdAt = createdAt,
            priority = priority,
            dueDateAndTime = dueDateAndTime,
            frequencyNumber = frequencyNumber,
            endDate = endDate,
            repeatAfterDone = repeatAfterDone,
        )
    }

    fun getAutoBackupTime(): Long {
        val c = Calendar.getInstance()
        c.add(Calendar.HOUR_OF_DAY, 1)
        return c.timeInMillis
    }

    fun sendNotification(context: Context, title: String, message: String) {
        val notificationId = NotificationIdProvider.nextId()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "default_channel_id")
            .setSmallIcon(R.mipmap.ic_launcher)   // your icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(context)) {
            // Check permission on Android 13+ before notifying
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(notificationId, builder.build())
            }
        }
    }

    object NotificationIdProvider {
        private var lastId = 0
        fun nextId(): Int = ++lastId
    }

    fun requestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                MaterialAlertDialogBuilder(context)
                    .setTitle("Allow precise reminders")
                    .setMessage("To make sure your alarms go off exactly on time, please allow this app to schedule exact alarms.")
                    .setPositiveButton("Continue") { _, _ ->
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = "package:${context.packageName}".toUri()
                        }
                        context.startActivity(intent)
                    }
                    .setNegativeButton("Not now", null)
                    .show()
            }
        }
    }

    fun doesTaskContainProperty(task: Task): Boolean {
        val emptyTask = getEmptyTask()
        return task.repeatType != emptyTask.repeatType || task.dueDateAndTime != emptyTask.dueDateAndTime || task.priority != emptyTask.priority
    }

    fun getNextMidNightMilli(): Long {
        val nextMidnight = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return nextMidnight.timeInMillis
    }
}