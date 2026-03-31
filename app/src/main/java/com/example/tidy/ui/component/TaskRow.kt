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

package com.example.tidy.ui.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun TaskRow(
    title: String,
    doneStatus: Boolean,
    showMenu: Boolean,
    onCloseMenu: () -> Unit,
    onOpenMenu: () -> Unit,
    menuContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {},
) {
    var tapOffset by remember { mutableStateOf(Offset.Zero) }
    Card(
        modifier = modifier
            .padding(8.dp)
            .heightIn(min = 35.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { offset ->
                        tapOffset = offset
                        onOpenMenu()
                    },
                    onTap = {
                        onTap()
                    }
                )
            },
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (doneStatus) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier
                .padding(8.dp)
        )
        TaskRowMenu(
            showMenu = showMenu,
            tapOffset = tapOffset,
            onDismiss = {
                onCloseMenu()
            }
        ) {
            menuContent()
        }
    }
}