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

import com.example.tidy.constants.RepeatTypes

data class TaskBackupDto(
    var id: Long = 0,
    var title: String,
    var done: Boolean = false,
    var repeatType: String = RepeatTypes.NONE,
    var repeatOn: String = "",
    var description: String? = null,
    var hide: Boolean = false,
    var parentId: Long? = null,
    var createdAt: Long = System.currentTimeMillis(),
)