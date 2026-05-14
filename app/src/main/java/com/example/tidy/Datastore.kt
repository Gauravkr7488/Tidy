package com.example.tidy

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(
    name = "settings"
)

val MIGRATED_TO_SQL = booleanPreferencesKey(
    "migrated_to_sql"
)