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

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany

@Entity
data class Task(
    @Id var id: Long = 0,
    var title: String,
    var done: Boolean = false,
    var repeat: Boolean = false,
    var hide: Boolean = false,
    var createdAt: Long = System.currentTimeMillis(),
){
    lateinit var children: ToMany<Task>
    lateinit var parents: ToMany<Task>
}

@Entity
data class LastReset(
    @Id var id: Long = 1,
    var lastResetAt: String
)