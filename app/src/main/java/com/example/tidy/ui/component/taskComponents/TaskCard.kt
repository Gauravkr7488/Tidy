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

package com.example.tidy.ui.component.taskComponents

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tidy.Task
import com.example.tidy.constants.RepeatTypes

@Composable
fun TaskCard(
    task: Task,
    modifier: Modifier = Modifier,
    onClick: (Task) -> Unit = {},
    leadingIcons: List<TaskIconAction> = emptyList(),
    trailingIcons: List<TaskIconAction> = emptyList(),
    contextMenuOptions: List<TaskContextAction> = emptyList(),
) {
    var tapOffset by remember { mutableStateOf(Offset.Zero) }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = if (!task.done) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick(task) },
                    onLongPress = { offset ->
                        if (contextMenuOptions.isNotEmpty()) {
                            tapOffset = offset
                            showMenu = true
                        }
                    }
                )
            },
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            leadingIcons.forEach { (icon, description, _, tint, modifier) ->
                Icon(
                    imageVector = icon,
                    contentDescription = description,
                    tint = tint,
                    modifier = modifier
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 16.dp, bottom = 16.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None,
                )
                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column { // For Badges
                if (task.children.isNotEmpty()) {
                    var doneChildrenCount = 0
                    task.children.forEach { child ->
                        if (child.done) doneChildrenCount++
                    }
                    Badge(
                        text = "${doneChildrenCount}/${task.children.size}",
                        imageVector = Icons.Default.TaskAlt,
                        contentDescription = "${doneChildrenCount}/${task.children.size} Done"
                    )
                }
                if (task.repeatType != RepeatTypes.NONE && task.repeatType != "none") {  // "none" to guard against old values
                    Badge(
                        text = task.repeatType,
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Repeats ${task.repeatType}"
                    )
                }
            }
            trailingIcons.forEach { (icon, description, _, tint, modifier) ->
                Icon(
                    imageVector = icon,
                    contentDescription = description,
                    tint = tint,
                    modifier = modifier
                )
            }
        }
        TaskContextMenu(
            showMenu = showMenu,
            tapOffset = tapOffset,
            onDismiss = { showMenu = !showMenu },
            options = contextMenuOptions
        )
    }
}

@Composable
fun Badge(text: String, imageVector: ImageVector, contentDescription: String) {
    Row(
        modifier = Modifier.width(80.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}