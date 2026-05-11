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

package com.example.tidy.ui.component.subTaskComponents

import androidx.compose.runtime.Composable
import com.example.tidy.ui.component.subTaskComponents.canvas.FullLine
import com.example.tidy.ui.component.subTaskComponents.canvas.HalfLine
import com.example.tidy.ui.component.subTaskComponents.canvas.HorizontalLine
import com.example.tidy.ui.component.subTaskComponents.canvas.NoLine

@Composable
fun AddIndentation(
    last: Boolean,
    map: List<Boolean>
) { // last is if the task is final child of the parent and map is the map of line and gaps excluding the final one cause a child always attaches to its parent
    map.forEach { b ->
        if (b) FullLine() else NoLine()
        NoLine() // for space of horizontalLine
    }
    if (last) HalfLine() else FullLine()
    HorizontalLine()
}