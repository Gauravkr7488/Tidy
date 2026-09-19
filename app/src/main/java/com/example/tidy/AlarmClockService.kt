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
import android.os.IBinder
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

    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint( "LaunchActivityFromNotification")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()   // onDestroy stops sound and vibration
            return START_NOT_STICKY
        }

        val taskName = intent?.getStringExtra(Options.TASK_NAME) ?: "Alarm"

        createChannel()

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
            .setContentIntent(stopIntent)             // tap body = dismiss
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
        const val NOTIF_ID = 1
        const val ACTION_STOP = "com.tidy.ACTION_STOP_ALARM"
    }
}