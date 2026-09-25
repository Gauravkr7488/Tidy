package com.example.tidy

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toUri
import androidx.core.content.edit

object AlarmPrefs {
    private const val PREFS = "alarm_prefs"
    private const val KEY_URI = "ringtone_uri"
    private const val KEY_TITLE = "ringtone_title"


    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getAlarmToneUri(context: Context): Uri {
        val saved = prefs(context).getString(KEY_URI, null)
        return saved?.toUri() ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    }


    fun getAlarmToneTitle(context: Context): String =
        prefs(context).getString(KEY_TITLE, null) ?: "Default"

    fun setAlarmTone(context: Context, uri: Uri, title: String) {
        prefs(context).edit {
            putString(KEY_URI, uri.toString())
                .putString(KEY_TITLE, title)
        }
    }

    fun getFileName(context: Context, uri: Uri): String =
        context.contentResolver
            .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { if (it.moveToFirst()) it.getString(0) else null }
            ?: "Custom sound"
}