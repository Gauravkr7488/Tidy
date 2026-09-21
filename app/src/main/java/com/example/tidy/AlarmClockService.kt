package com.example.tidy

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.Ace777.tidy.R
import com.example.tidy.constants.Options

class AlarmClockService : Service() {
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    private val handler = Handler(Looper.getMainLooper())
    private var taskName = "Task"
    private val autoStop = Runnable {
        Utils.sendNotification(
            context = this,
            title = "Missed Alarm",
            message = "Schedule for $taskName met",
            notificationId = NOTIF_ID + 1
        )
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("LaunchActivityFromNotification")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()   // onDestroy stops sound and vibration
            return START_NOT_STICKY
        }

        taskName = intent?.getStringExtra(Options.TASK_NAME) ?: "Alarm"
        val taskId = intent?.getLongExtra(Options.TASK_ID, -1)

        createChannel()

        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                action = ACTION_OPEN_ALARM
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(Options.TASK_ID, taskId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, AlarmClockService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(taskName)
            .setContentText("Tap to open")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(openIntent)
            .setDeleteIntent(stopIntent)
            .addAction(0, "Dismiss", stopIntent)
            .setOngoing(true)
            .build()

        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        } else {
            0
        }

        ServiceCompat.startForeground(this, NOTIF_ID, notification, serviceType)
        startRinging()
        handler.removeCallbacks(autoStop)                    // reset if already scheduled
        handler.postDelayed(autoStop, RING_DURATION_MS) // stop after 5 min
        return START_NOT_STICKY
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun startRinging() {
        if (player != null) return

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM) // plays even in silent/DND-alarm mode
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(this@AlarmClockService, uri)
            isLooping = true
            prepare()
            start()
        }

        vibrator = if (Build.VERSION.SDK_INT >= 31)
            getSystemService(VibratorManager::class.java).defaultVibrator
        else
            @Suppress("DEPRECATION") getSystemService(Vibrator::class.java)

        vibrator?.vibrate(
            VibrationEffect.createWaveform(
                longArrayOf(0, 800, 800),
                0
            )
        ) // repeat from index 0
    }

    override fun onDestroy() {
        handler.removeCallbacks(autoStop)
        player?.run { stop(); release() }
        player = null
        vibrator?.cancel()
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Alarms", NotificationManager.IMPORTANCE_HIGH)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "alarm_channel"
        const val NOTIF_ID = 1000 // to prevent clash with other channel
        const val ACTION_STOP = "com.tidy.ACTION_STOP_ALARM"
        const val ACTION_OPEN_ALARM = "com.tidy.ACTION_OPEN_ALARM"
        const val RING_DURATION_MS = 5 * 60 * 1000L
    }
}