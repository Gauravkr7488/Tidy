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

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun TaskRowMenu(
    showMenu: Boolean,
    tapOffset: Offset,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { onDismiss() },
        offset = with(density) {
            DpOffset(tapOffset.x.toDp(), tapOffset.y.toDp())
        },
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        content()
// pass this stuff when called

//        DropdownMenuItem(
//            text = {
//                Text(
//                    "Edit",
//                    style = MaterialTheme.typography.bodyMedium,
//                    fontWeight = FontWeight.Medium
//                )
//            },
//            leadingIcon = {
//                Icon(
//                    imageVector = Icons.Outlined.Edit,
//                    contentDescription = null,
//                    modifier = Modifier.size(18.dp)
//                )
//            },
//            onClick = {
//                showContextMenu = false
//                addTaskViewModel.setCurrentTaskId(task.id)
//                navController.navigate(Routes.ADD_TASK)
//            }
//        )

    }
}