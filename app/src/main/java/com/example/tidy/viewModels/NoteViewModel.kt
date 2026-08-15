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
package com.example.tidy.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tidy.NoteService
import com.tidy.sqldelight.Note
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(private val noteService: NoteService) : ViewModel() {

    /** All notes, ordered by most-recently-updated first. */
    val notes = noteService.observeNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addNote(title: String, body: String) {
        viewModelScope.launch {
            noteService.insert(title.trim(), body.trim())
        }
    }

    fun updateNote(id: Long, title: String, body: String) {
        viewModelScope.launch {
            noteService.update(id, title.trim(), body.trim())
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            noteService.delete(id)
        }
    }
}
