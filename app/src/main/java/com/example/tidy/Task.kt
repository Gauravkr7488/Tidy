package com.example.tidy

import android.R
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class Task(
    @Id var id: Long = 0,
    var title: String,
    var done: Boolean = false,
    var repeat: Boolean = false,
    var hide: Boolean = false,
    var createdAt: Long = System.currentTimeMillis(),
)

@Entity
data class LastReset(
    @Id var id: Long = 1,
    var lastResetAt: String
)