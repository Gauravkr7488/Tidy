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

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.tidy.sqldelight.Note
import com.yourapp.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NoteService(private val db: AppDatabase) {

    fun observeNotes(): Flow<List<Note>> =
        db.noteQueries.getAll()
            .asFlow()
            .mapToList(Dispatchers.IO)

    suspend fun insert(title: String, body: String): Unit = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        db.noteQueries.insert(
            title = title,
            body = body,
            createdAt = now,
            updatedAt = now
        )
    }

    suspend fun update(id: Long, title: String, body: String): Unit = withContext(Dispatchers.IO) {
        db.noteQueries.update(
            id = id,
            title = title,
            body = body,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun delete(id: Long): Unit = withContext(Dispatchers.IO) {
        db.noteQueries.delete(id)
    }
}
